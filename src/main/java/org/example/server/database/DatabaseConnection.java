package org.example.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/error"
    );
    private static final String USER = System.getenv().getOrDefault(
            "DB_USER", "root"
    );
    private static final String PASSWORD = System.getenv().getOrDefault(
            "DB_PASSWORD", ""
    );

    private static Connection instance;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }
}