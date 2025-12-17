@echo off
REM HPMS Database Setup Script for Windows/XAMPP
REM This script helps you set up the MySQL database for HPMS

echo ========================================
echo HPMS Database Setup
echo ========================================
echo.

REM Check if MySQL is running
echo Checking if MySQL is running...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL is running
) else (
    echo [WARNING] MySQL might not be running!
    echo Please start MySQL from XAMPP Control Panel
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 1: Create Database
echo ========================================
echo.
echo This will:
echo - Drop existing hpms_db database (if any)
echo - Create fresh hpms_db database
echo - Create all tables
echo - Insert default data
echo.

set /p confirm="Do you want to continue? (y/n): "
if /i not "%confirm%"=="y" (
    echo Setup cancelled
    exit /b 0
)

echo.
echo Running database schema script...
echo.

REM Run the SQL script
"C:\xampp\mysql\bin\mysql.exe" -u root < ..\database\database_schema.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database created successfully!
    echo.
    echo ========================================
    echo Step 2: Verify Database
    echo ========================================
    echo.
    echo Opening MySQL command line to verify...
    echo You can run: USE hpms_db; SHOW TABLES;
    echo Type 'exit' to quit MySQL
    echo.
    pause
    "C:\xampp\mysql\bin\mysql.exe" -u root hpms_db
) else (
    echo.
    echo [ERROR] Failed to create database
    echo.
    echo Possible issues:
    echo 1. MySQL not running - Start it from XAMPP Control Panel
    echo 2. Wrong MySQL path - Check if MySQL is at C:\xampp\mysql\
    echo 3. Access denied - You might need a password
    echo.
    echo To run manually:
    echo C:\xampp\mysql\bin\mysql.exe -u root ^< ..\database\database_schema.sql
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Open project in VS Code
echo 2. Review DATABASE_MIGRATION_GUIDE.md
echo 3. Test database connection with DBConnection.java
echo 4. Start migrating services to use database
echo.
pause
