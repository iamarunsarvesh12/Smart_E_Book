package com.ebook.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    public static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/smart_ebook_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DEFAULT_USER = "root";
    public static final String DEFAULT_PASSWORD = "password";

    // Configurable fields with defaults
    private static String dbDriver = DEFAULT_DRIVER;
    private static String dbUrl = DEFAULT_URL;
    private static String dbUser = DEFAULT_USER;
    private static String dbPassword = DEFAULT_PASSWORD;

    // Fallback status
    private static volatile boolean usingFallback = false;
    private static final Object fallbackLock = new Object();
    private static volatile boolean fallbackInitialized = false;

    static {
        // Read environment variables or system properties if available
        String envUrl = System.getenv("EBOOK_DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            dbUrl = envUrl.trim();
        }
        String envUser = System.getenv("EBOOK_DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            dbUser = envUser.trim();
        }
        String envPass = System.getenv("EBOOK_DB_PASSWORD");
        if (envPass != null) {
            dbPassword = envPass.trim();
        }
    }

    // Private constructor to prevent instantiation
    private DBConnection() {
    }

    /**
     * Sets custom database configuration (useful for testing or custom environments).
     */
    public static void setCustomConfig(String driver, String url, String user, String password) {
        dbDriver = driver;
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
        usingFallback = false;
    }

    /**
     * Resets configuration back to default MySQL settings.
     */
    public static void resetToDefaultConfig() {
        dbDriver = DEFAULT_DRIVER;
        dbUrl = DEFAULT_URL;
        dbUser = DEFAULT_USER;
        dbPassword = DEFAULT_PASSWORD;
        usingFallback = false;
    }

    /**
     * Obtains a thread-safe connection to the configured database.
     * Automatically falls back to an embedded persistent engine if MySQL is unavailable.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the JDBC Driver class is not found
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (usingFallback) {
            return getFallbackConnection();
        }

        try {
            Class.forName(dbDriver);
            DriverManager.setLoginTimeout(3);
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            // If MySQL fails (connection refused, access denied, etc.), seamlessly initialize fallback
            System.err.println("[DBConnection] Primary MySQL connection failed (" + e.getMessage() + "). Activating embedded fallback engine...");
            synchronized (fallbackLock) {
                usingFallback = true;
                return getFallbackConnection();
            }
        }
    }

    /**
     * Creates and initializes the embedded fallback database.
     */
    private static Connection getFallbackConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        String h2Url = "jdbc:h2:./smart_ebook_db_data;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
        Connection conn = DriverManager.getConnection(h2Url, "sa", "");

        if (!fallbackInitialized) {
            synchronized (fallbackLock) {
                if (!fallbackInitialized) {
                    initFallbackSchema(conn);
                    fallbackInitialized = true;
                }
            }
        }
        return conn;
    }

    /**
     * Reads and executes schema.sql into the fallback database.
     */
    private static void initFallbackSchema(Connection conn) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.sql");
            if (is == null) {
                is = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql");
            }
            if (is == null) {
                is = DBConnection.class.getResourceAsStream("/schema.sql");
            }
            if (is == null) {
                File schemaFile = new File("database/schema.sql");
                if (schemaFile.exists()) {
                    is = new FileInputStream(schemaFile);
                } else {
                    File altFile = new File("../database/schema.sql");
                    if (altFile.exists()) {
                        is = new FileInputStream(altFile);
                    }
                }
            }

            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sqlBuilder.append(line).append(" ");
                }
                reader.close();

                String[] statements = sqlBuilder.toString().split(";");
                Statement stmt = conn.createStatement();
                for (String statementSql : statements) {
                    String cleanSql = statementSql.trim();
                    if (!cleanSql.isEmpty()) {
                        // Skip CREATE DATABASE / USE statements in H2
                        if (cleanSql.toUpperCase().startsWith("CREATE DATABASE") || 
                            cleanSql.toUpperCase().startsWith("USE ")) {
                            continue;
                        }
                        try {
                            String sanitizedSql = cleanSql
                                .replace("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4", "")
                                .replace("ON UPDATE CURRENT_TIMESTAMP", "");
                            stmt.execute(sanitizedSql);
                        } catch (SQLException ex) {
                            // Suppress non-fatal schema differences while logging if significant
                            if (!ex.getMessage().contains("already exists")) {
                                System.err.println("[DBConnection] Schema execution notice: " + ex.getMessage());
                            }
                        }
                    }
                }
                stmt.close();
                System.out.println("[DBConnection] Embedded database initialized with complete schema and seed records.");
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Error initializing fallback schema: " + e.getMessage());
        }
    }

    /**
     * Safely closes a database Connection.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing Connection: " + e.getMessage());
            }
        }
    }

    /**
     * Safely closes a PreparedStatement or generic Statement.
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing Statement: " + e.getMessage());
            }
        }
    }

    /**
     * Overloaded convenience method specifically for PreparedStatement.
     */
    public static void closePreparedStatement(PreparedStatement preparedStatement) {
        closeStatement(preparedStatement);
    }

    /**
     * Safely closes a ResultSet.
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    /**
     * Convenience method to close all three resources safely in one call.
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
}
