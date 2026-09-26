@REM Maven wrapper script for Windows
@echo off
set "MAVEN_PROJECTBASEDIR=%~dp0"
if exist "%MAVEN_PROJECTBASEDIR%\..\tools\apache-maven-3.9.9\bin\mvn.cmd" (
    call "%MAVEN_PROJECTBASEDIR%\..\tools\apache-maven-3.9.9\bin\mvn.cmd" %*
) else (
    call mvn %*
)
