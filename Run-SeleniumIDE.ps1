param([string]$Node='node', [string]$BaseUrl='http://127.0.0.1:8081')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    if(-not (Test-Path '.local/selenium-ide/node_modules/selenium-side-runner')) {throw 'Install the IDE runner first: npm install --cache .local/npm-cache --prefix .local/selenium-ide selenium-side-runner@4.0.13'}
    $driver=Get-ChildItem '.local/selenium-cache/chromedriver' -Recurse -Filter chromedriver.exe -ErrorAction SilentlyContinue | Select-Object -First 1
    if(-not $driver){throw 'Run Verify-Web.ps1 once so Selenium Manager can obtain the matching ChromeDriver.'}
    $env:Path=$driver.Directory.FullName+';'+$env:Path
    & $Node web/tests/run_ide.js -w 1 --base-url $BaseUrl --config-file web/tests/ide-config.yml --output-directory output/stqa/ide --screenshot-failure-directory output/stqa/ide/failures web/tests/Gym_STQA.side
    if($LASTEXITCODE -ne 0){throw 'A Selenium IDE replay test failed. See output/stqa/ide.'}
} finally {Pop-Location}
