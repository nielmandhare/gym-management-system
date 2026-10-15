param(
    [string]$MySqlBin = 'C:\Program Files\MySQL\MySQL Server 8.0\bin',
    [switch]$OpenBrowser
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    function Get-ListenerOwners([int]$PortNumber) {
        try {
            $taskListeners = @(Get-NetTCPConnection -LocalPort $PortNumber -State Listen -ErrorAction Stop)
        } catch {
            if ($_.FullyQualifiedErrorId -like 'CmdletizationQuery_NotFound,*') { return @() }
            throw
        }
        foreach ($taskListener in ($taskListeners | Sort-Object OwningProcess -Unique)) {
            $taskOwner = Get-CimInstance Win32_Process -Filter "ProcessId=$($taskListener.OwningProcess)"
            if (-not $taskOwner) { throw "Could not identify the listener on port $PortNumber." }
            $taskOwner
        }
    }
    $expectedDatabaseConfig = ([IO.Path]::GetFullPath((Join-Path $PSScriptRoot '.local/mysql/my.ini'))).Replace('\','/')
    $databaseOwners = @(Get-ListenerOwners 3307)
    foreach ($taskOwner in $databaseOwners) {
        $taskCommand = ([string]$taskOwner.CommandLine).Replace('\','/')
        if ($taskOwner.Name -ine 'mysqld.exe' -or $taskCommand.IndexOf($expectedDatabaseConfig, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
            throw "Port 3307 belongs to another or unidentified service (PID $($taskOwner.ProcessId)). This project will not connect to or stop that service."
        }
    }
    $expectedWebClasses = ([IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'build/web'))).Replace('\','/')
    $webOwners = @(Get-ListenerOwners 8081)
    foreach ($taskOwner in $webOwners) {
        $taskCommand = ([string]$taskOwner.CommandLine).Replace('\','/')
        if ($taskOwner.Name -ine 'java.exe' -or -not $taskCommand.Contains('web.GymWebServer') -or $taskCommand.IndexOf($expectedWebClasses, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
            throw "Port 8081 belongs to another service (PID $($taskOwner.ProcessId)). Choose a free web port with Run-Web.ps1 -Port."
        }
    }
    if ($databaseOwners.Count -eq 0) {
        & (Join-Path $PSScriptRoot 'Setup-Database.ps1') -MySqlBin $MySqlBin
    }
    if ($webOwners.Count -eq 0) {
        & (Join-Path $PSScriptRoot 'Run-Web.ps1') -Background
    }
    $taskResponse = Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:8081/' -TimeoutSec 15
    if ($taskResponse.StatusCode -ne 200) { throw 'The gym web app did not respond successfully.' }
    Write-Host "Project: $PSScriptRoot"
    Write-Host 'Gym web app is running at http://127.0.0.1:8081/'
    Write-Host 'Admin: admin / 123 | Trainer: trainer / 123 | Member: user1 / 123'
    if ($OpenBrowser) { Start-Process 'http://127.0.0.1:8081/' }
} finally { Pop-Location }
