param([string]$BaseUrl='http://127.0.0.1:8081', [string]$DriverPath)
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    $taskJavaHome=$env:JAVA_HOME
    if (-not $taskJavaHome -or -not (Test-Path (Join-Path $taskJavaHome 'bin/javac.exe'))) {
        $taskJdk=Get-ChildItem (Join-Path $env:ProgramFiles 'Java/jdk-*') -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
        if ($taskJdk) {$taskJavaHome=$taskJdk.FullName}
    }
    if (-not $taskJavaHome) {throw 'Set JAVA_HOME to an installed JDK 17 or newer.'}
    $jar=Join-Path $PSScriptRoot '.local/selenium-java/selenium-server-4.51.0.jar'
    if (-not (Test-Path $jar)) {
        New-Item -ItemType Directory -Force (Split-Path $jar) | Out-Null
        Invoke-WebRequest 'https://github.com/SeleniumHQ/selenium/releases/download/selenium-4.51.0/selenium-server-4.51.0.jar' -OutFile $jar
    }
    if (-not $DriverPath) {
        $taskDriver=Get-ChildItem '.local/selenium-cache/chromedriver' -Filter chromedriver.exe -Recurse -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($taskDriver) {$DriverPath=$taskDriver.FullName}
    }
    if (-not $DriverPath -or -not (Test-Path -LiteralPath $DriverPath)) {throw 'Run Verify-Web.ps1 once to obtain a matching ChromeDriver, or supply -DriverPath.'}
    $classes=Join-Path $PSScriptRoot 'build/java-selenium'
    $socketDirectory=Join-Path $PSScriptRoot '.local/java-sockets'
    New-Item -ItemType Directory -Force $classes,$socketDirectory | Out-Null
    & (Join-Path $taskJavaHome 'bin/javac.exe') -encoding UTF-8 -proc:none -cp $jar -d $classes 'web/tests/java/src/main/java/GymWebSmoke.java'
    if($LASTEXITCODE -ne 0){throw 'Java Selenium compilation failed.'}
    & (Join-Path $taskJavaHome 'bin/java.exe') "-Djdk.net.unixdomain.tmpdir=$socketDirectory" -cp "$classes;$jar" GymWebSmoke $BaseUrl $PSScriptRoot $DriverPath
    if($LASTEXITCODE -ne 0){throw 'Java Selenium smoke suite failed. See output/stqa/selenium_java_results.json.'}
} finally {Pop-Location}
