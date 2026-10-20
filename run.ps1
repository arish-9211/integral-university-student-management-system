param([switch]$Rebuild, [switch]$NoSeed)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExecutable = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $installedJdk = Get-ChildItem 'C:\Program Files\Java\jdk*' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if ($installedJdk) { $env:JAVA_HOME=$installedJdk.FullName; $javaExecutable=Join-Path $env:JAVA_HOME 'bin\java.exe' }
    else { $javaCommand=Get-Command java.exe -ErrorAction SilentlyContinue; if(-not $javaCommand){throw 'Install JDK 17 or newer and reopen VS Code.'};$javaExecutable=$javaCommand.Source }
}
$jarPath = if (Test-Path 'target\student-management-system.jar') { 'target\student-management-system.jar' } else { 'release\student-management-system.jar' }
if ($Rebuild -or -not (Test-Path $jarPath)) {
    & '.\mvnw.cmd' clean package
    if($LASTEXITCODE -ne 0) { throw 'Build failed. Review the Maven output above.' }
    $jarPath = 'target\student-management-system.jar'
}
Write-Host 'Starting Student Management System. Open http://localhost:8080 (or your PORT setting).'
Write-Host 'Press Ctrl+C to stop. MongoDB must be running; see README.md.'
if($NoSeed) { & $javaExecutable -jar $jarPath }
else { & $javaExecutable -jar $jarPath --seed }
exit $LASTEXITCODE
