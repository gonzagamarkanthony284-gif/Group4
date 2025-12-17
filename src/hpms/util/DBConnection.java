package hpms.util;

import hpms.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class
 * Uses DatabaseConfig for connection settings
 */
public class DBConnection {
    
    public static Connection getConnection() {
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
            return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL, 
                DatabaseConfig.JDBC_USER, 
                DatabaseConfig.JDBC_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
