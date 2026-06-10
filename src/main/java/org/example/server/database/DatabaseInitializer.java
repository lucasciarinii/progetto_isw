package org.example.server.database;

import org.example.server.ServerLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ensures the game database and schema exist at server startup.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    /**
     * Creates the database and core schema if they do not exist.
     *
     * @throws SQLException when initialization fails
     */
    public static void ensureDatabase() throws SQLException {
        loadDriver();
        String url = DatabaseConnection.getUrl();
        String user = DatabaseConnection.getUser();
        String password = DatabaseConnection.getPassword();

        DbUrlParts parts = parseUrl(url);
        boolean dbExists = databaseExists(parts, user, password);
        if (dbExists) {
            ServerLogger.server("Database already exists. Using existing database.");
        } else {
            ServerLogger.server("Database not found. Creating empty database...");
        }

        createDatabaseIfMissing(parts, user, password);
        createSchemaIfMissing(url, user, password);

        if (!dbExists) {
            ServerLogger.server("Database created successfully.");
        }
    }

    /**
     * Checks whether the target database exists on the DBMS.
     *
     * @param parts parsed URL components (server URL and database name)
     * @param user database user
     * @param password database password
     * @return true if the database already exists
     * @throws SQLException when the check fails
     */
    private static boolean databaseExists(DbUrlParts parts, String user, String password) throws SQLException {
        String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (Connection connection = DriverManager.getConnection(parts.serverUrl(), user, password);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, parts.dbName());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Loads the MySQL JDBC driver to ensure DriverManager can resolve connections.
     *
     * @throws SQLException when the driver is not available
     */
    private static void loadDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath", e);
        }
    }

    /**
     * Splits a JDBC URL into server URL and database name components.
     *
     * @param url the JDBC URL configured for the server
     * @return parsed URL parts
     * @throws SQLException when the URL does not match the expected MySQL format
     */
    private static DbUrlParts parseUrl(String url) throws SQLException {
        String prefix = "jdbc:mysql://";
        if (!url.startsWith(prefix)) {
            throw new SQLException("Unsupported DB_URL format: " + url);
        }

        int hostStart = prefix.length();
        int slashIndex = url.indexOf('/', hostStart);
        if (slashIndex < 0) {
            throw new SQLException("Unsupported DB_URL format: " + url);
        }

        String hostPort = url.substring(hostStart, slashIndex);
        String dbAndParams = url.substring(slashIndex + 1);

        String dbName = dbAndParams;
        String params = "";
        int questionIndex = dbAndParams.indexOf('?');
        if (questionIndex >= 0) {
            dbName = dbAndParams.substring(0, questionIndex);
            params = dbAndParams.substring(questionIndex);
        }

        if (dbName.isBlank()) {
            throw new SQLException("Unsupported DB_URL format: " + url);
        }

        String serverUrl = "jdbc:mysql://" + hostPort + params;
        return new DbUrlParts(serverUrl, dbName);
    }

    /**
     * Creates the database if it does not already exist.
     *
     * @param parts parsed URL components (server URL and database name)
     * @param user database user
     * @param password database password
     * @throws SQLException when database creation fails
     */
    @SuppressWarnings("SqlSourceToSinkFlow") // The database name is validated in parseUrl, so this is safe.
    private static void createDatabaseIfMissing(DbUrlParts parts, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(parts.serverUrl(), user, password);
             Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE IF NOT EXISTS `" + parts.dbName() + "`");
            }
    }

    /**
     * Creates the required tables and view if they do not already exist.
     *
     * @param url JDBC URL that includes the target database name
     * @param user database user
     * @param password database password
     * @throws SQLException when schema creation fails
     */

    private static void createSchemaIfMissing(String url, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS game (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    num_players INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS game_result (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    game_id INT NOT NULL,
                    nickname VARCHAR(64) NOT NULL,
                    final_score INT NOT NULL,
                    in_game_placement INT NOT NULL,
                    CONSTRAINT fk_game_result_game
                        FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
                )
                """);

            //noinspection SqlCaseVsIf
            statement.execute("""
                CREATE OR REPLACE VIEW ranking_view AS
                SELECT
                    gr.nickname AS nickname,
                    g.num_players AS num_players,
                    SUM(CASE WHEN gr.in_game_placement = 1 THEN 1 ELSE 0 END) AS wins,
                    AVG(gr.final_score) AS avg_score,
                    DENSE_RANK() OVER (
                        PARTITION BY g.num_players
                        ORDER BY
                            SUM(CASE WHEN gr.in_game_placement = 1 THEN 1 ELSE 0 END) DESC,
                            AVG(gr.final_score) DESC
                    ) AS rank_position
                FROM game_result gr
                INNER JOIN game g ON g.id = gr.game_id
                GROUP BY g.num_players, gr.nickname
                """);
        }
    }

    /**
     * Java record -> Nested type: light immutable class with 2 fields
     * It serves for CONTAINING the parsed server URL and database name.
     *
     * @param serverUrl JDBC URL without the database name
     * @param dbName target database name
     */
    private record DbUrlParts(String serverUrl, String dbName) {
    }
}
