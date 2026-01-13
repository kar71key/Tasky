@echo off
REM Tasky Run Script - Runs the compiled application

setlocal EnableDelayedExpansion

REM Try to find Java
set JAVA_HOME=C:\Program Files\Java\jdk-25
if not exist "%JAVA_HOME%\bin\java.exe" (
    for /d %%i in ("C:\Program Files\Java\jdk-*") do set JAVA_HOME=%%i
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Could not find JDK. Please set JAVA_HOME environment variable.
    exit /b 1
)

REM Check if JAR exists
if not exist "target\tasky.jar" (
    echo Application not built yet. Running build.bat first...
    call build.bat
    if errorlevel 1 exit /b 1
)

REM Run the application
echo Starting Tasky...
"%JAVA_HOME%\bin\java.exe" -cp "target\tasky.jar;lib\gson-2.10.1.jar" com.tasky.Main
