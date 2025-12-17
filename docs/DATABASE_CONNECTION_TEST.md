# Database Connection Test Guide

## Quick Tests to Verify Database Connection

### Method 1: Run the Database Connection Test (Easiest)

The project includes a test class to verify the connection:

**Option A: Run via Eclipse**
1. Open `src/hpms/test/DatabaseConnectionTest.java`
2. Right-click → Run As → Java Application
3. Check the console output

**Option B: Run via Command Line**
```bash
cd C:\Users\LEGION\eclipse-workspace\HPMS
javac -cp "lib\*;src" -d bin src\hpms\test\DatabaseConnectionTest.java
java -cp "lib\*;bin" hpms.test.DatabaseConnectionTest
```

**Expected Output (Success):**
```
✓ Database connection successful!
✓ Connected to: hpms_db
✓ MySQL Server Version: [version number]
✓ Connection test passed!
```

**Expected Output (Failure):**
```
✗ Database connection failed!
Error: [error message]
```

### Method 2: Check MySQL Service Status

**Via XAMPP Control Panel:**
1. Open XAMPP Control Panel
2. Check if MySQL shows "Running" (green)
3. If not running, click "Start" next to MySQL

**Via Command Line:**
```bash
tasklist | findstr mysqld
```
If you see `mysqld.exe` in the output, MySQL is running.

### Method 3: Test via phpMyAdmin

1. Open XAMPP Control Panel
2. Click "Admin" button next to MySQL (opens phpMyAdmin)
3. If phpMyAdmin opens successfully, MySQL is running
4. Check if `hpms_db` database exists in the left sidebar
5. If database exists, connection is working

### Method 4: Test via MySQL Command Line

```bash
cd C:\xampp\mysql\bin
mysql.exe -u root
```

If you get a MySQL prompt (`mysql>`), the connection works.

Then check if the database exists:
```sql
SHOW DATABASES;
USE hpms_db;
SHOW TABLES;
```

### Method 5: Test via Application Login

1. Run the application: `scripts\run_hpms.bat`
2. Try to login with admin account:
   - Username: `admin`
   - Password: `admin123`
3. If login succeeds, database is connected and working
4. If you get "Database connection failed" error, check MySQL status

### Method 6: Check Application Logs

Look for error messages in:
- Console output when starting the application
- `app.log` file (if logging is enabled)
- `app_error.log` file (if errors occur)

## Common Connection Issues

### Issue 1: "MySQL JDBC Driver not found!"
**Solution:**
- Make sure `mysql-connector-j-9.5.0.jar` is in the `lib/` folder
- Check your classpath includes `lib\*`

### Issue 2: "Database connection failed!"
**Possible Causes:**
- MySQL service not running
- Wrong username/password in `DBConnection.java`
- Database `hpms_db` doesn't exist
- Wrong port (should be 3306)

**Solutions:**
1. Start MySQL in XAMPP Control Panel
2. Verify credentials in `src/hpms/util/DBConnection.java`
3. Run `scripts\setup_database.bat` to create the database
4. Check if port 3306 is correct

### Issue 3: "Access denied for user 'root'@'localhost'"
**Solution:**
- Check username/password in `DatabaseConfig.java`
- For XAMPP default: USER = "root", PASS = ""
- If you set a password, update `PASS` in `DBConnection.java`

### Issue 4: "Unknown database 'hpms_db'"
**Solution:**
- Run `scripts\setup_database.bat` to create the database
- Or manually create: `CREATE DATABASE hpms_db;`

## Step-by-Step Verification Checklist

Use this checklist to verify everything is working:

- [ ] MySQL service is running (XAMPP Control Panel shows "Running")
- [ ] Can access phpMyAdmin (http://localhost/phpmyadmin)
- [ ] Database `hpms_db` exists in phpMyAdmin
- [ ] Tables exist in `hpms_db` (users, patients, staff, etc.)
- [ ] `DBConnection.java` has correct credentials
- [ ] `mysql-connector-j-9.5.0.jar` is in `lib/` folder
- [ ] DatabaseConnectionTest runs successfully
- [ ] Application login works (admin/admin123)

## Quick Test Script

You can also create a simple test by running this in your main method:

```java
import hpms.util.DBConnection;
import java.sql.Connection;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✓ Connection successful!");
            try {
                conn.close();
            } catch (Exception e) {}
        } else {
            System.out.println("✗ Connection failed!");
        }
    }
}
```

## Visual Indicators in Application

When the application starts successfully:
- Login window appears
- No error messages in console
- Can login with admin account

When database is not connected:
- Error dialog appears
- Console shows "Database connection failed!"
- Login fails even with correct credentials

## Next Steps After Verification

Once connection is verified:
1. Login as admin
2. Create test accounts via Administration panel
3. Test patient registration
4. Verify data is being saved to database (check phpMyAdmin)

