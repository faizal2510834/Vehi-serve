package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    
    private static DBConnection instance;
    private Properties properties;

    private DBConnection() {
        properties = new Properties();
        // Load from project root config/db.properties
        File configFile = new File("config/db.properties");
        if (!configFile.exists()) {
            System.err.println("Configuration file not found: " + configFile.getAbsolutePath());
            System.err.println("Please run the application from the project root directory.");
            throw new RuntimeException("Database configuration not found. Run from project root.");
        }
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error reading database configuration: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        
        return DriverManager.getConnection(url, user, password);
    }
}
