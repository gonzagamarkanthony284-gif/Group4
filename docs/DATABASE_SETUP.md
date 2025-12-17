# Database Setup Guide

## Default XAMPP Configuration

If you're using **XAMPP** (most common setup), the default MySQL credentials are:

- **Username:** `root`
- **Password:** (empty/blank - leave as `""`)

## How to Configure Database Connection

Edit `src/hpms/config/DatabaseConfig.java`:

```java
public static final String JDBC_USER = "root";        // Your MySQL username
public static final String JDBC_PASSWORD = "";        // Your MySQL password (empty for XAMPP default)
public static final String JDBC_URL = "jdbc:mysql://localhost:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
```

## Testing Your Database Connection

### Option 1: Test via phpMyAdmin
1. Open XAMPP Control Panel
2. Start MySQL service
3. Click "Admin" next to MySQL (opens phpMyAdmin)
4. Try logging in with:
   - Username: `root`
   - Password: (leave blank)
   
If this works, use the same credentials in `DBConnection.java`.

### Option 2: Test via MySQL Command Line
1. Open Command Prompt
2. Navigate to XAMPP MySQL: `cd C:\xampp\mysql\bin`
3. Try connecting:
   ```bash
   mysql.exe -u root
   ```
   (No password needed for default XAMPP)

If this works, your credentials are `root` with empty password.

### Option 3: Test via Setup Script
Run the setup script:
```bash
scripts\setup_database.bat
```

If it works, your credentials are correct.

## If You Have a Password Set

If you've set a MySQL password (or using a different MySQL installation):

1. **Update DBConnection.java:**
   ```java
   private static final String USER = "root";        // Your username
   private static final String PASS = "yourpassword"; // Your password
   ```

2. **Update setup_database.bat** (if needed):
   Change line 46 from:
   ```bat
   "C:\xampp\mysql\bin\mysql.exe" -u root < ..\database\database_schema.sql
   ```
   To:
   ```bat
   "C:\xampp\mysql\bin\mysql.exe" -u root -pyourpassword < ..\database\database_schema.sql
   ```

## Common Scenarios

### Scenario 1: Fresh XAMPP Installation
- **USER:** `root`
- **PASS:** `""` (empty string)

### Scenario 2: XAMPP with Password Set
- **USER:** `root`
- **PASS:** `"yourpassword"` (the password you set)

### Scenario 3: Remote Database
- **URL:** `jdbc:mysql://your-server-ip:3306/hpms_db?useSSL=false&serverTimezone=UTC`
- **USER:** (provided by your hosting)
- **PASS:** (provided by your hosting)

### Scenario 4: Different MySQL Installation
- Check your MySQL configuration
- Use the credentials you set during MySQL installation

## Troubleshooting

### "Access Denied" Error
- Check if username/password is correct
- Verify MySQL is running (XAMPP Control Panel)
- Try connecting via phpMyAdmin first

### "Connection Refused" Error
- Make sure MySQL service is started in XAMPP
- Check if port 3306 is correct
- Verify database `hpms_db` exists (run `setup_database.bat`)

### "Database not found" Error
- Run `scripts\setup_database.bat` to create the database
- Or manually create: `CREATE DATABASE hpms_db;`

## Quick Test

To quickly test your connection, you can run:
```bash
scripts\setup_database.bat
```

If it completes successfully, your credentials are correct!

