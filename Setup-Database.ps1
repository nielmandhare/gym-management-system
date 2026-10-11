param([string]$MySqlBin = 'C:\Program Files\MySQL\MySQL Server 8.0\bin')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $server = Join-Path $MySqlBin 'mysqld.exe'
    $client = Join-Path $MySqlBin 'mysql.exe'
    function Test-MySQLReady([string[]]$ClientArgs) {
        # Windows PowerShell treats native stderr as terminating with Stop;
        # a stopped database is expected during this readiness probe.
        $ErrorActionPreference = 'Continue'
        & $client @ClientArgs 2>$null | Out-Null
        return ($LASTEXITCODE -eq 0)
    }
    if (!(Test-Path $server)) { throw "MySQL server binaries not found at $MySqlBin" }
    $basePath = (Split-Path $MySqlBin).Replace('\','/')
    $projectPath = $PSScriptRoot.Replace('\','/')
    New-Item -ItemType Directory -Force '.local/mysql' | Out-Null
    $newDatabase = !(Test-Path '.local/mysql/data/mysql')
    if ($newDatabase) {
        & $server --no-defaults --initialize-insecure "--basedir=$basePath" "--datadir=$projectPath/.local/mysql/data" --console
        if ($LASTEXITCODE -ne 0) { throw 'MySQL data initialization failed.' }
    }
    @"
[mysqld]
basedir=$basePath
datadir=$projectPath/.local/mysql/data
port=3307
bind-address=127.0.0.1
mysqlx=0
log-error=$projectPath/.local/mysql/server.log
"@ | Set-Content '.local/mysql/my.ini' -Encoding ASCII
    @'
[client]
user=root
password=1234
host=127.0.0.1
port=3307
'@ | Set-Content '.local/mysql/root.cnf' -Encoding ASCII
    $rootConfig = Join-Path $PSScriptRoot '.local/mysql/root.cnf'
    $config = Join-Path $PSScriptRoot '.local/mysql/my.ini'
    $expectedConfig = [System.IO.Path]::GetFullPath($config)
    try {
        $listeners = @(Get-NetTCPConnection -LocalPort 3307 -State Listen -ErrorAction Stop)
    } catch {
        if ($_.FullyQualifiedErrorId -like 'CmdletizationQuery_NotFound,*') {
            $listeners = @()
        } else {
            throw
        }
    }
    foreach ($listener in $listeners) {
        $owner = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener.OwningProcess)" -ErrorAction SilentlyContinue
        $isThisProjectServer = $owner -and $owner.Name -ieq 'mysqld.exe' -and
            ([string]$owner.CommandLine).IndexOf($expectedConfig, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        if (!$isThisProjectServer) {
            $ownerName = if ($owner) { "$($owner.Name) (PID $($owner.ProcessId))" } else { "unknown process (PID $($listener.OwningProcess))" }
            throw "Port 3307 is already in use by $ownerName. Setup will not connect to that service. Stop the conflict yourself or reconfigure the project port before setup."
        }
    }
    if (!(Test-MySQLReady -ClientArgs @("--defaults-extra-file=$rootConfig",'--connect-timeout=2','--execute=SELECT 1'))) {
        Start-Process -FilePath $server -ArgumentList ('--defaults-file="'+$config+'" --standalone') -WindowStyle Hidden | Out-Null
        $ready=$false
        for ($i=0; $i -lt 30; $i++) {
            Start-Sleep -Milliseconds 500
            if ($newDatabase) {
                $ready=Test-MySQLReady -ClientArgs @('--no-defaults','--host=127.0.0.1','--port=3307','--user=root','--connect-timeout=1','--execute=SELECT 1')
            } else {
                $ready=Test-MySQLReady -ClientArgs @("--defaults-extra-file=$rootConfig",'--connect-timeout=1','--execute=SELECT 1')
            }
            if ($ready) { break }
        }
        if (!$ready) { throw 'Dedicated MySQL instance did not start. Check .local/mysql/server.log and whether port 3307 is occupied.' }
    }
    if ($newDatabase) {
        & $client --no-defaults --host=127.0.0.1 --port=3307 --user=root --execute="ALTER USER 'root'@'localhost' IDENTIFIED BY '1234';"
        if ($LASTEXITCODE -ne 0) { throw 'Local root password setup failed.' }
    }
    & $client "--defaults-extra-file=$rootConfig" --execute="CREATE USER IF NOT EXISTS 'gms_dev'@'localhost' IDENTIFIED BY '1234'; GRANT ALL PRIVILEGES ON gms.* TO 'gms_dev'@'localhost';"
    if ($LASTEXITCODE -ne 0) { throw 'Local database user setup failed.' }
    # Seed once per local instance: launching must not overwrite edited profiles,
    # account status, memberships, products or new progress records.
    $seedMarker = Join-Path $PSScriptRoot '.local/mysql/demo-seed-v1.complete'
    $imports = @('gms-database-init.sql')
    if (!(Test-Path -LiteralPath $seedMarker)) { $imports += 'local-demo-seed.sql' }
    foreach ($sql in $imports) {
        & $client "--defaults-extra-file=$rootConfig" "--execute=source $projectPath/backups/$sql"
        if ($LASTEXITCODE -ne 0) { throw "Import failed: $sql" }
        if ($sql -eq 'local-demo-seed.sql') { 'Demo seed installed successfully.' | Set-Content -LiteralPath $seedMarker -Encoding ASCII }
    }
    # Back up the current schema/data before installing the incremental account table.
    $dump = Join-Path $MySqlBin 'mysqldump.exe'
    $migrationBackup = Join-Path $PSScriptRoot ('backups/pre-user-accounts_' + (Get-Date -Format 'yyyyMMdd_HHmmss') + '.sql')
    & $client "--defaults-extra-file=$rootConfig" --batch --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='gms' AND table_name='app_user'" | ForEach-Object { $accountTableExists = [int]$_ }
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect account migration state.' }
    if (!$accountTableExists) {
        & $dump "--defaults-extra-file=$rootConfig" --no-tablespaces --single-transaction "--result-file=$migrationBackup" gms
        if ($LASTEXITCODE -ne 0) { throw 'Pre-migration backup failed; migration cancelled.' }
    }
    & $client "--defaults-extra-file=$rootConfig" "--execute=source $projectPath/backups/user-accounts-migration.sql"
    if ($LASTEXITCODE -ne 0) { throw 'Account migration failed.' }
    & $client "--defaults-extra-file=$rootConfig" --batch --skip-column-names --execute="SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='gms' AND table_name='app_user' AND column_name='member_id'" | ForEach-Object { $memberLinkExists = [int]$_ }
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect three-role migration state.' }
    if (!$memberLinkExists) {
        $rbacBackup = Join-Path $PSScriptRoot ('backups/pre-three-role_' + (Get-Date -Format 'yyyyMMdd_HHmmss') + '.sql')
        & $dump "--defaults-extra-file=$rootConfig" --no-tablespaces --single-transaction "--result-file=$rbacBackup" gms
        if ($LASTEXITCODE -ne 0) { throw 'Three-role backup failed; migration cancelled.' }
    }
    & $client "--defaults-extra-file=$rootConfig" "--execute=source $projectPath/backups/three-role-migration.sql"
    if ($LASTEXITCODE -ne 0) { throw 'Three-role migration failed.' }
    & $client "--defaults-extra-file=$rootConfig" "--execute=source $projectPath/backups/role-demo-accounts.sql"
    if ($LASTEXITCODE -ne 0) { throw 'Role demo account seed failed.' }
    if (!(Test-Path 'src/resources/db.properties')) {
        @"
db.host=127.0.0.1
db.port=3307
db.name=gms
db.user=gms_dev
db.password=1234
db.mysql_path=$($MySqlBin.Replace('\','/'))/mysql.exe
db.mysqldump_path=$($MySqlBin.Replace('\','/'))/mysqldump.exe
"@ | Set-Content 'src/resources/db.properties' -Encoding ASCII
    }
    if (!(Test-Path 'src/resources/mysql.cnf')) {
        @'
[client]
user=gms_dev
password=1234
host=127.0.0.1
port=3307
'@ | Set-Content 'src/resources/mysql.cnf' -Encoding ASCII
    }
    Write-Host 'MySQL ready: gms at 127.0.0.1:3307. Application login: admin / 123.'
} finally { Pop-Location }
