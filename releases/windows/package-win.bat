@echo off
echo ===================================================
echo   Kerala Police ERP - Windows Packaging Script
echo ===================================================

:: 1. Build the Jar
echo [1/3] Compiling and Packaging JAR...
call mvn clean package -DskipTests

:: 2. Create Icon (Requires icon.png in current dir)
:: Recommended: Use a tool like Magick or an online converter to make KeralaPolice.ico
if not exist "KeralaPolice.ico" (
    echo [!] KeralaPolice.ico not found. Using default Java icon.
)

:: 3. Generate EXE
echo [2/3] Generating Native EXE Installer...
:: Note: Requires WiX Toolset installed on Windows
jpackage --type exe ^
  --name "KeralaPoliceERP" ^
  --description "Official Kerala Police Command Center ERP" ^
  --vendor "Kerala Police" ^
  --input target ^
  --main-jar crime-management-system-1.0.0.jar ^
  --main-class com.crimemanagement.DesktopLauncher ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu ^
  --dest target/dist ^
  --icon "KeralaPolice.ico"

echo [3/3] Done! Installer created in target/dist/
pause
