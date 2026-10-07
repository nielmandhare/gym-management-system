# Instructions for AI coding agents

## Goal

This is a Java Swing + JDBC + MySQL desktop project. Treat replication as creating and running an independent checkout on the current machine. Do not assume the original developer's Windows username, Desktop path, installed MySQL path, or local database files exist on another computer.

## Clone into a separate checkout

Use the repository URL supplied by the user or the Git hosting page. Do not guess a remote URL. Pick a new destination on the current machine; do not use the original developer's absolute path or overwrite an existing folder.

```powershell
$repositoryUrl = Read-Host 'Paste the repository URL'
$checkoutPath = Read-Host 'Enter a new checkout directory'
git clone $repositoryUrl $checkoutPath
Set-Location -LiteralPath $checkoutPath
```

All build, database, configuration, and log files should stay inside this checkout. The scripts use `$PSScriptRoot` for project paths. The local MySQL data directory is `.local/mysql/data`; generated connection files are `src/resources/db.properties` and `src/resources/mysql.cnf`; build outputs and logs are under `build/`, `dist/`, and `logs/`. These machine-local paths are ignored by Git.

## Before starting MySQL

The supplied database setup is for Windows and uses a dedicated local MySQL instance bound to `127.0.0.1:3307`. It expects MySQL 8.x executables under `C:\Program Files\MySQL\MySQL Server 8.0\bin` by default. The ordinary MySQL service on port 3306 is separate.

**Check port 3307 before running `Setup-Database.ps1` or `Run.ps1`. If a listener is not this checkout's own `mysqld.exe` using this checkout's `.local/mysql/my.ini`, stop and ask the user how to proceed. Do not connect to, initialize, modify, or stop an existing service to make this project work.** `Setup-Database.ps1` enforces this ownership check before it connects. The project scripts use a fixed port, so do not try another database port unless the user requests the required configuration changes.

```powershell
$probe = [System.Net.Sockets.TcpClient]::new()
try {
    $probe.Connect('127.0.0.1', 3307)
    throw 'Port 3307 is occupied. Do not run project database setup.'
} catch [System.Net.Sockets.SocketException] {
    Write-Host 'Port 3307 is available.'
} finally {
    $probe.Dispose()
}
```

The setup script initializes only `.local/mysql/data` within the checkout, but the fixed port check is important: an unrelated server could otherwise be mistaken for the project's instance. Never run broad process kills, delete a database directory outside the current checkout, or reuse another project's `.local` directory.

## Requirements and first run

- Windows PowerShell, JDK 17 or newer (`java` and `javac` on `PATH`), and MySQL Server 8.x binaries.
- Internet access on the first build so `Build.ps1` can download Apache Ant 1.10.15 launcher JARs into `.local/tools/`. Global Ant and NetBeans are not required by the supplied scripts.

After confirming port 3307 is available and MySQL is installed in the default location:

```powershell
powershell -ExecutionPolicy Bypass -File .\Run.ps1
```

`Run.ps1` runs `Setup-Database.ps1`, builds the JAR, and launches the application. For an installation in another folder, `Setup-Database.ps1` accepts `-MySqlBin`; because `Run.ps1` calls setup with its default path, run setup with the correct path first, then build and launch directly:

```powershell
powershell -ExecutionPolicy Bypass -File .\Setup-Database.ps1 -MySqlBin 'C:\path\to\MySQL\bin'
powershell -ExecutionPolicy Bypass -File .\Build.ps1
java -jar .\dist\Gym_Management_System.jar
```

The local demo app login is `admin` / `1234` (mobile `0770000001` / `1234`). The local database accounts are `gms_dev` / `1234` and `root` / `1234`; these are demo-only credentials. Do not reuse them for real services.

## Agent boundaries

- Make code or documentation changes only in the current checkout and only within the user's requested scope.
- Do not push, publish, or send files to external services unless the user explicitly asks.
- Do not copy local credential files, database directories, generated backups, logs, or build outputs between computers.
- Before destructive cleanup, resolve and verify the target path is inside the current checkout.
- If setup fails because a requirement is missing, report the exact missing requirement rather than altering another local service or hard-coding the original machine's paths.

See `README.md` for the project features, database schema, local demo setup, report templates, and recorded verification details.
