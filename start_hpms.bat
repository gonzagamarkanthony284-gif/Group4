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

REM Use Java 8 explicitly
"C:\Program Files\Java\jre1.8.0_471\bin\java.exe" -Xmx512m -cp "lib\*;bin" hpms.app.Launcher

echo.
echo Application closed.
pause
