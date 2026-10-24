package com.studyplanner.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe Database Connection utility for MySQL JDBC.
 * Reads db.properties from classpath and supports environment variable overrides.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static Properties properties = new Properties();
    private static boolean driverLoaded = false;

    static {
        loadProperties();
        loadDriver();
    }

    private static void loadProperties() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
                LOGGER.info("db.properties loaded successfully.");
            } else {
                LOGGER.warning("db.properties not found on classpath, using defaults.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load db.properties, falling back to defaults", e);
        }
    }

    private static void loadDriver() {
        String driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
            driverLoaded = true;
            LOGGER.info("MySQL JDBC Driver registered: " + driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Could not load MySQL JDBC Driver: " + driver, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }

        // Support environment variable overrides
        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/ai_study_planner_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        }

        String user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = properties.getProperty("db.user", "root");
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = properties.getProperty("db.password", "Harshi@09");
        }

        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed: " + e.getMessage(), e);
            return false;
        }
    }
}
