$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force '.local/tools' | Out-Null
    foreach ($name in @('ant','ant-launcher')) {
        if (!(Test-Path ".local/tools/$name.jar")) {
            Invoke-WebRequest "https://repo.maven.apache.org/maven2/org/apache/ant/$name/1.10.15/$name-1.10.15.jar" -OutFile ".local/tools/$name.jar"
        }
    }
    & java -cp '.local/tools/*' org.apache.tools.ant.Main -quiet clean jar
    if ($LASTEXITCODE -ne 0) { throw 'Ant build failed. Use JDK 17 or newer (java and javac must be available).' }
} finally { Pop-Location }
