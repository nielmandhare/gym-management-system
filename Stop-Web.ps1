$ErrorActionPreference = 'Stop'
$pidFile = Join-Path $PSScriptRoot '.local/web-server.pid'
if (-not (Test-Path -LiteralPath $pidFile)) { Write-Host 'No background gym web server was recorded.'; return }
$webProcessId = [int](Get-Content -LiteralPath $pidFile)
$process = Get-CimInstance Win32_Process -Filter "ProcessId=$webProcessId" -ErrorAction SilentlyContinue
if (-not $process) { Remove-Item -LiteralPath $pidFile; Write-Host 'The recorded server is already stopped.'; return }
$expected = Join-Path $PSScriptRoot 'build/web'
if ($process.Name -ne 'java.exe' -or -not ([string]$process.CommandLine).Contains('web.GymWebServer') -or -not ([string]$process.CommandLine).Contains($expected)) { throw 'The recorded PID does not match this checkout web server. It will not be stopped.' }
Stop-Process -Id $webProcessId
Remove-Item -LiteralPath $pidFile
Write-Host 'This checkout web server stopped.'
