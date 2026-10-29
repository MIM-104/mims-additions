@echo off
setlocal
title MIM's Additions - Development Client

rem Gradle 9 needs a modern JDK; Minecraft uses Gradle's Java 8 toolchain.
if exist "%ProgramFiles%\Java\jdk-25\bin\java.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-25"
) else if exist "%USERPROFILE%\.jdks\openjdk-25.0.1\bin\java.exe" (
    set "JAVA_HOME=%USERPROFILE%\.jdks\openjdk-25.0.1"
) else (
    echo Java 25 could not be found. Install it or update JAVA_HOME in this file.
    pause
    exit /b 1
)

cd /d "%~dp0"
if errorlevel 1 (
    echo Could not open the mod project folder.
    pause
    exit /b 1
)

echo Starting MIM's Additions development client...
call "%~dp0gradlew.bat" runClient %*
set "CLIENT_EXIT_CODE=%ERRORLEVEL%"
if not "%CLIENT_EXIT_CODE%"=="0" (
    echo.
    echo The development client stopped with error %CLIENT_EXIT_CODE%.
    pause
)
exit /b %CLIENT_EXIT_CODE%
