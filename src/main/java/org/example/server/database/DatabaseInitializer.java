package org.example.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ensures the game database and schema exist at server startup.
 */
public final class DatabaseInitializer {

    private static final Pattern DB_URL_PATTERN =
            Pattern.compile("^jdbc:mysql://([^/]+)/([^?]+)(\\?.*)?$");

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
        createDatabaseIfMissing(parts, user, password);
        createSchemaIfMissing(url, user, password);
    }

    private static void loadDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath", e);
        }
    }

    private static DbUrlParts parseUrl(String url) throws SQLException {
        Matcher matcher = DB_URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new SQLException("Unsupported DB_URL format: " + url);
        }
        String hostPort = matcher.group(1);
        String dbName = matcher.group(2);
        String params = matcher.group(3) == null ? "" : matcher.group(3);
        String serverUrl = "jdbc:mysql://" + hostPort + params;
        return new DbUrlParts(serverUrl, dbName);
    }

    private static void createDatabaseIfMissing(DbUrlParts parts, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(parts.serverUrl(), user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + parts.dbName() + "`");
        }
    }

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

    private record DbUrlParts(String serverUrl, String dbName) {
    }
}
