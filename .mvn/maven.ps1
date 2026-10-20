$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $env:JAVA_HOME) {
    $installedJdk = Get-ChildItem 'C:\Program Files\Java\jdk*' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if ($installedJdk) { $env:JAVA_HOME = $installedJdk.FullName }
}
$mavenVersion = '3.9.11'
$mavenDirectory = Join-Path $projectRoot ".tools\apache-maven-$mavenVersion"
$mavenExecutable = Join-Path $mavenDirectory 'bin\mvn.cmd'
if (-not (Test-Path -LiteralPath $mavenExecutable)) {
    $systemMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($systemMaven) { & $systemMaven.Source @args; exit $LASTEXITCODE }
    New-Item -ItemType Directory -Force (Join-Path $projectRoot '.tools') | Out-Null
    $archive = Join-Path $projectRoot '.tools\maven.zip'
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    Write-Host "Downloading Maven $mavenVersion from Apache Maven Central..."
    Invoke-WebRequest -UseBasicParsing $url -OutFile $archive
    $checksumContent = (Invoke-WebRequest -UseBasicParsing "$url.sha512").Content
    $checksumText = if ($checksumContent -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($checksumContent) } else { [string]$checksumContent }
    $expected = $checksumText.Trim().Split(' ')[0]
    $hasher = [System.Security.Cryptography.SHA512]::Create()
    $stream = [System.IO.File]::OpenRead($archive)
    try { $actual = [System.BitConverter]::ToString($hasher.ComputeHash($stream)).Replace('-', '') }
    finally { $stream.Dispose(); $hasher.Dispose() }
    if ($actual -ne $expected) { throw 'Maven checksum verification failed.' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($archive, (Join-Path $projectRoot '.tools'))
    Remove-Item -LiteralPath $archive
}
& $mavenExecutable @args
exit $LASTEXITCODE
