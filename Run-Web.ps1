param([int]$Port = 8081, [switch]$Background)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $javaHome = $env:JAVA_HOME
    if (-not $javaHome -or -not (Test-Path -LiteralPath (Join-Path $javaHome 'bin/javac.exe'))) {
        $jdk = Get-ChildItem (Join-Path $env:ProgramFiles 'Java/jdk-*') -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
        if ($jdk) { $javaHome = $jdk.FullName }
    }
    if ($javaHome -and (Test-Path -LiteralPath (Join-Path $javaHome 'bin/javac.exe'))) {
        $javac = Join-Path $javaHome 'bin/javac.exe'
        $java = Join-Path $javaHome 'bin/java.exe'
    } else {
        $javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
        $java = (Get-Command java -ErrorAction SilentlyContinue).Source
    }
    if (-not $javac -or -not $java) { throw 'JDK 17 or newer is required (java and javac were not found).' }
    if ($Port -lt 1024 -or $Port -gt 65535) { throw 'Choose a web port between 1024 and 65535.' }
    $env:GMS_WEB_PORT = [string]$Port
    # JasperReports 6.21 expects OpenPDF's API. It must precede legacy iText JARs.
    $libraries = (Join-Path $PSScriptRoot 'lib/openpdf-1.3.30.jar') + ';' + (Join-Path $PSScriptRoot 'lib/*')
    $connector = Join-Path $PSScriptRoot 'lib/mysql-connector-j-8.4.0.jar'
    if (-not (Test-Path -LiteralPath $connector)) { throw "MySQL Connector/J is missing: $connector" }
    $output = Join-Path $PSScriptRoot 'build/web'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    & $javac -encoding UTF-8 -proc:none -cp $libraries -d $output 'src/security/Passwords.java' 'web/GymWebServer.java' 'web/WebReports.java'
    if ($LASTEXITCODE -ne 0) { throw 'Web application compilation failed.' }
    $socketDirectory = Join-Path $PSScriptRoot '.local/java-sockets'
    New-Item -ItemType Directory -Force -Path $socketDirectory | Out-Null
    # Java 17 places selector sockets in the Windows TEMP directory by default.
    # Keeping that path local also lets the JDK fall back to TCP for long paths.
    if ($Background) {
        $logDirectory = Join-Path $PSScriptRoot 'logs'
        New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
        $arguments = @('"-Djdk.net.unixdomain.tmpdir=' + $socketDirectory + '"', '-cp', '"' + $output + ';' + $libraries + '"', 'web.GymWebServer')
        $process = Start-Process -FilePath $java -ArgumentList $arguments -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logDirectory 'web-server.out.log') -RedirectStandardError (Join-Path $logDirectory 'web-server.error.log')
        $process.Id | Set-Content -LiteralPath (Join-Path $PSScriptRoot '.local/web-server.pid') -Encoding ASCII
        Start-Sleep -Seconds 2
        if ($process.HasExited) { throw 'Web server could not start. See logs/web-server.error.log.' }
        Write-Host "Gym web app: http://127.0.0.1:$Port (PID $($process.Id))."
    } else {
        & $java "-Djdk.net.unixdomain.tmpdir=$socketDirectory" -cp "$output;$libraries" web.GymWebServer
        if ($LASTEXITCODE -ne 0) { throw 'Web server stopped with an error.' }
    }
} finally { Pop-Location }
