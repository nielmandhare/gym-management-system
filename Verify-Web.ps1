param([string]$BaseUrl='http://127.0.0.1:8081', [string]$Python='python', [ValidateSet('chrome','edge')][string]$Browser='chrome')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    & $Python -c "import sys; assert sys.version_info >= (3,10), 'Python 3.10 or newer required'"
    if($LASTEXITCODE -ne 0){throw 'Python 3.10 or newer is required. Supply -Python with the executable path.'}
    if(-not (Test-Path '.local/selenium/selenium')) {
        & $Python -m pip install --cache-dir '.local/pip-cache' --target '.local/selenium' -r web/tests/requirements.txt
        if($LASTEXITCODE -ne 0){throw 'Could not install Selenium test dependencies.'}
    }
    $env:PYTHONIOENCODING='utf-8'
    $env:SE_CACHE_PATH=Join-Path $PSScriptRoot '.local/selenium-cache'
    & $Python web/tests/selenium_suite.py --base-url $BaseUrl --browser $Browser
    if($LASTEXITCODE -ne 0){throw 'A browser regression case failed. See output/stqa/runs and latest-run.txt.'}
} finally {Pop-Location}
