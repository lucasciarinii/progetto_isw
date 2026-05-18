package org.example.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a singleton JDBC connection configured via environment variables.
 */
public class DatabaseConnection {

    /** Database URL from environment or default. */
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/error"
    );
    /** Database user from environment or default. */
    private static final String USER = System.getenv().getOrDefault(
            "DB_USER", "root"
    );
    /** Database password from environment or default. */
    private static final String PASSWORD = System.getenv().getOrDefault(
            "DB_PASSWORD", ""
    );

    /** Cached singleton connection instance. */
    private static Connection instance;

    /** Instantiation. */
    private DatabaseConnection() {}

    /**
     * Returns a live JDBC connection, creating it if needed.
     *
     * @return JDBC connection
     * @throws SQLException when the connection cannot be created
     */
    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                // Ensure the MySQL driver class is loaded and registered with DriverManager.
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found on classpath", e);
            }
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }
}