$ErrorActionPreference = 'Stop'
$config = Join-Path $PSScriptRoot '.local/mysql/root.cnf'
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqladmin.exe' "--defaults-extra-file=$config" shutdown
if ($LASTEXITCODE -ne 0) { throw 'Could not stop the dedicated MySQL instance.' }
