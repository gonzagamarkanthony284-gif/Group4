@echo off
REM HPMS System Launcher - One Click to Run Everything
REM This script starts the HPMS application automatically

echo ========================================
echo HPMS Hospital Management System Launcher
echo ========================================
echo.

REM Check if Java is installed
echo Checking Java installation...
where /Q java.exe
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if MySQL is running
echo Checking MySQL server...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if errorlevel 1 (
    echo WARNING: MySQL may not be running!
    echo Starting XAMPP MySQL may be required
    timeout /t 3
)

REM Check if database exists
echo Verifying database...
"C:\xampp\mysql\bin\mysql.exe" -u root -e "USE hpms_db;" 2>NUL
if errorlevel 1 (
    echo ERROR: Database hpms_db not found!
    echo Please run setup_database.bat first
    pause
    exit /b 1
)

echo.
echo ========================================
echo Starting HPMS Application...
echo ========================================
echo.

REM Stop any existing Java processes
taskkill /FI "IMAGENAME eq java.exe" /F 2>NUL

REM Wait a moment
timeout /t 2 /nobreak

REM Start the application
cd /d "%~dp0"
"C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin\java.exe" -cp "lib\*;bin" hpms.app.Launcher

REM If application closes, show this message
echo.
echo ========================================
echo HPMS Application Closed
echo ========================================
pause
