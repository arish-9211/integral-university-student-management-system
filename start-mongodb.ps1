$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$mongoCommand = Get-Command mongod.exe -ErrorAction SilentlyContinue
if ($mongoCommand) { $mongoExecutable=$mongoCommand.Source }
else {
    $mongoFile=Get-ChildItem 'C:\Program Files\MongoDB\Server\*\bin\mongod.exe' -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
    if(-not $mongoFile){throw 'MongoDB Community Server was not found. Install it using the README link.'}
    $mongoExecutable=$mongoFile.FullName
}
New-Item -ItemType Directory -Force '.data\mongodb' | Out-Null
Write-Host 'Starting a local MongoDB on port 27017. Keep this terminal open. Press Ctrl+C to stop.'
Write-Host 'If port 27017 is already in use by MongoDB, use the existing service instead.'
& $mongoExecutable --dbpath (Join-Path $PSScriptRoot '.data\mongodb') --bind_ip 127.0.0.1 --port 27017
