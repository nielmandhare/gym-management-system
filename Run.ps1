$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & "$PSScriptRoot/Setup-Database.ps1"
    & "$PSScriptRoot/Build.ps1"
    & java -jar 'dist/Gym_Management_System.jar'
    if ($LASTEXITCODE -ne 0) { throw 'Application exited with an error; inspect logs/logger.log.' }
} finally { Pop-Location }
