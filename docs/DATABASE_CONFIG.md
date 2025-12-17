# Database Configuration Guide

## Configuration Location

Database connection settings are centralized in:
**`src/hpms/config/DatabaseConfig.java`**

This follows best practices by separating configuration from implementation.

## Default Configuration

For XAMPP default installation:

```java
public static final String JDBC_URL = "jdbc:mysql://localhost:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
public static final String JDBC_USER = "root";
public static final String JDBC_PASSWORD = "";  // Empty for XAMPP default
public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
```

## How to Update Configuration

### For XAMPP Default (No Password)
```java
public static final String JDBC_USER = "root";
public static final String JDBC_PASSWORD = "";  // Leave empty
```

### For XAMPP with Password Set
```java
public static final String JDBC_USER = "root";
public static final String JDBC_PASSWORD = "yourpassword";  // Your MySQL password
```

### For Custom MySQL Installation
```java
public static final String JDBC_URL = "jdbc:mysql://localhost:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
public static final String JDBC_USER = "your_username";
public static final String JDBC_PASSWORD = "your_password";
```

### For Remote Database
```java
public static final String JDBC_URL = "jdbc:mysql://your-server-ip:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
public static final String JDBC_USER = "remote_username";
public static final String JDBC_PASSWORD = "remote_password";
```

## Configuration Options Explained

### JDBC_URL Parameters

- `localhost:3306` - MySQL server address and port
- `hpms_db` - Database name
- `useSSL=false` - Disable SSL (for local development)
- `serverTimezone=UTC` - Set timezone to UTC
- `connectTimeout=5000` - Connection timeout (5 seconds)
- `socketTimeout=5000` - Socket timeout (5 seconds)

### JDBC_DRIVER

The driver class name. For MySQL Connector/J 8.0+:
```java
public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
```

## Security Best Practices

⚠️ **Important for Production:**

1. **Never commit credentials to version control**
   - Use environment variables
   - Use configuration files excluded from git
   - Use secure credential management

2. **Example with environment variables:**
   ```java
   public static final String JDBC_USER = System.getenv("DB_USER");
   public static final String JDBC_PASSWORD = System.getenv("DB_PASSWORD");
   ```

3. **Use different credentials for development and production**

## Testing Configuration

After updating configuration, test it:

1. Run `src/hpms/test/DatabaseConnectionTest.java`
2. Or try logging into the application
3. Check console for connection errors

## Troubleshooting

### "Access denied" Error
- Verify `JDBC_USER` and `JDBC_PASSWORD` are correct
- Test credentials via phpMyAdmin first

### "Connection refused" Error
- Check if MySQL is running
- Verify `JDBC_URL` host and port are correct
- Check firewall settings

### "Unknown database" Error
- Verify database name in `JDBC_URL` matches your database
- Run `scripts\setup_database.bat` to create the database

## Migration from Old Configuration

If you were using the old `DBConnection.java` with hardcoded values:

1. **Old way** (in DBConnection.java):
   ```java
   private static final String USER = "root";
   private static final String PASS = "";
   ```

2. **New way** (in DatabaseConfig.java):
   ```java
   public static final String JDBC_USER = "root";
   public static final String JDBC_PASSWORD = "";
   ```

The `DBConnection.java` now automatically uses `DatabaseConfig`, so no other code changes are needed!

