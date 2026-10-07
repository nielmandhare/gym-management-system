$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & "$PSScriptRoot/Setup-Database.ps1"
    & "$PSScriptRoot/Build.ps1"
    New-Item -ItemType Directory -Force 'work/test' | Out-Null
    & javac -proc:none -cp 'dist/Gym_Management_System.jar;dist/lib/*' -d 'work/test' 'test/LocalVerification.java'
    if ($LASTEXITCODE -ne 0) { throw 'Verification harness compilation failed.' }
    & java -cp 'work/test;dist/Gym_Management_System.jar;dist/lib/*' LocalVerification
    if ($LASTEXITCODE -ne 0) { throw 'Integration verification failed.' }
} finally { Pop-Location }
