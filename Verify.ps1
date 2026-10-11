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
    & javac -proc:none -cp 'dist/Gym_Management_System.jar;dist/lib/*' -d 'work/test' 'test/AccountVerification.java'
    if ($LASTEXITCODE -ne 0) { throw 'Account verification compilation failed.' }
    & java -cp 'work/test;dist/Gym_Management_System.jar;dist/lib/*' AccountVerification
    if ($LASTEXITCODE -ne 0) { throw 'Account feature verification failed.' }
    & javac -encoding UTF-8 -proc:none -cp 'dist/Gym_Management_System.jar;dist/lib/*' -d 'work/test' 'test/ThreeRoleVerification.java'
    if ($LASTEXITCODE -ne 0) { throw 'Three-role verification compilation failed.' }
    & java -cp 'work/test;dist/Gym_Management_System.jar;dist/lib/*' ThreeRoleVerification
    if ($LASTEXITCODE -ne 0) { throw 'Three-role integration verification failed.' }
} finally { Pop-Location }
