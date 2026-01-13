@echo off
REM Tasky Build Script - Compiles and packages the application
REM Requires JDK 11+ to be installed

setlocal EnableDelayedExpansion

REM Try to find Java
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
if not exist "%JAVA_HOME%\bin\javac.exe" (
    for /d %%i in ("C:\Program Files\Java\jdk-*") do set "JAVA_HOME=%%i"
)
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo ERROR: Could not find JDK. Please set JAVA_HOME environment variable.
    exit /b 1
)

echo Using Java from: %JAVA_HOME%

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

REM Create output directories
if not exist "target\classes" mkdir "target\classes"
if not exist "lib" mkdir "lib"

REM Download Gson if not present
if not exist "lib\gson-2.10.1.jar" (
    echo Downloading Gson library...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile 'lib\gson-2.10.1.jar'"
    if errorlevel 1 (
        echo ERROR: Failed to download Gson. Please download manually from:
        echo https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
        echo and place it in the lib folder.
        exit /b 1
    )
)

REM Compile all Java files
echo Compiling Java sources...
"%JAVA_HOME%\bin\javac.exe" -d "target\classes" -cp "lib\gson-2.10.1.jar" -sourcepath "src\main\java" "src\main\java\com\tasky\Main.java" "src\main\java\com\tasky\model\Priority.java" "src\main\java\com\tasky\model\Task.java" "src\main\java\com\tasky\model\TaskList.java" "src\main\java\com\tasky\model\AppData.java" "src\main\java\com\tasky\service\DataService.java" "src\main\java\com\tasky\service\TaskService.java" "src\main\java\com\tasky\ui\MainFrame.java" "src\main\java\com\tasky\ui\SidebarPanel.java" "src\main\java\com\tasky\ui\TaskPanel.java" "src\main\java\com\tasky\ui\TaskEditorDialog.java" "src\main\java\com\tasky\ui\ListEditorDialog.java"
if errorlevel 1 (
    echo ERROR: Compilation failed.
    exit /b 1
)

REM Create manifest
echo Main-Class: com.tasky.Main> "target\MANIFEST.MF"
echo Class-Path: lib/gson-2.10.1.jar>> "target\MANIFEST.MF"

REM Create JAR
echo Creating JAR file...
cd "target\classes"
"%JAVA_HOME%\bin\jar.exe" cfm "..\tasky.jar" "..\MANIFEST.MF" *
cd ..\..

echo.
echo Build successful!
echo Run the application with: run.bat
echo Or: java -jar target\tasky.jar
