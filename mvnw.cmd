@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0.mvn\maven.ps1" %*
exit /b %errorlevel%
