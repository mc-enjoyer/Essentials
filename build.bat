@echo off
echo Building Essentials Plugin Suite...
echo.

REM Clean and build all modules
echo Building all modules...
call mvn clean install -Dmaven.test.skip=true

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Copying JAR files to build folder...

REM Create build directory
if not exist "build" mkdir build

REM Copy JAR files only
copy "Essentials\target\Essentials-2.x-SNAPSHOT.jar" "build\"
copy "EssentialsAntiBuild\target\EssentialsAntiBuild-2.x-SNAPSHOT.jar" "build\"
copy "EssentialsChat\target\EssentialsChat-2.x-SNAPSHOT.jar" "build\"
copy "EssentialsProtect\target\EssentialsProtect-2.x-SNAPSHOT.jar" "build\"
copy "EssentialsSpawn\target\EssentialsSpawn-2.x-SNAPSHOT.jar" "build\"

echo.
echo Build complete! All JAR files are in the 'build' folder.
echo.
pause 