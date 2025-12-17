package hpms.config;

/**
 * Central place for DB configuration. Replace the placeholders with
 * your actual MySQL connection details. Keep credentials out of source
 * control in a real deployment.
 */
public final class DatabaseConfig {
    // Database connection URL
    // Format: jdbc:mysql://host:port/database?options
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/hpms_db?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
    
    // Database username
    // For XAMPP default: "root"
    // For custom MySQL installation: use your MySQL username
    public static final String JDBC_USER = "root";
    
    // Database password
    // For XAMPP default: "" (empty string)
    // For custom MySQL installation: use your MySQL password
    public static final String JDBC_PASSWORD = "";
    
    // JDBC Driver class name
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Database name (for reference)
    public static final String DATABASE_NAME = "hpms_db";
    
    // Prevent instantiation
    private DatabaseConfig() {
        // Utility class - no instances allowed
    }
}

