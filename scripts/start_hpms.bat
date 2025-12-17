@echo off
REM HPMS System Launcher with Java 8
cd /d C:\xampp\htdocs\HPMS
echo.
echo ============================================================
echo  HPMS - Hospital Patient Management System
echo ============================================================
echo.
echo Starting application...
echo.

REM Use Java 17 explicitly
"C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin\java.exe" -Xmx512m -cp "lib\*;bin" hpms.app.Launcher

echo.
echo Application closed.
pause
