@echo off
REM HPMS System Launcher - Simplified Version
echo.
echo ============================================================
echo  HPMS - Hospital Patient Management System
echo ============================================================
echo.
echo Starting application with MySQL database...
echo Database: hpms_db
echo.

REM Use explicit Java 8 path if available, otherwise use system java
set JAVA_CMD=java

REM Navigate to HPMS directory
cd /d C:\xampp\htdocs\HPMS

REM Run the application
%JAVA_CMD% -cp "lib\*;bin" hpms.app.Launcher

echo.
echo Application closed.
pause
