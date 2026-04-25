package org.example.server.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameDAO {

    /**
     * Save a complete game on a DB
     * @param numPlayers number of players in the game
     * @param results    maps nickname -> final score
     * @param placements maps nickname -> final position (1 = first)
     */

    public void saveGame(int numPlayers, Map<String, Integer> results, Map<String, Integer> placements) throws SQLException {

        Connection conn = DatabaseConnection.getInstance();
        conn.setAutoCommit(false); // transazione atomica

        try {
            // 1) Insert the game and get the generated ID
            int gameId;
            String insertGame = "INSERT INTO game (num_players) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertGame, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, numPlayers);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                gameId = keys.getInt(1);
            }

            // 2) Inserisce i risultati di ogni giocatore
            String insertResult = "INSERT INTO game_result (game_id, nickname, final_score, in_game_placement) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertResult)) {
                for (Map.Entry<String, Integer> entry : results.entrySet()) {
                    String nickname = entry.getKey();
                    ps.setInt(1, gameId);
                    ps.setString(2, nickname);
                    ps.setInt(3, entry.getValue());
                    ps.setInt(4, placements.get(nickname));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Restituisce la classifica globale per partite con un dato numero di giocatori,
     * ordinata per punteggio decrescente.
     */
    public List<RankingEntry> getRanking(int numPlayers) throws SQLException {
        String query = """
                SELECT gr.nickname, gr.final_score, g.game_date, g.num_players, gr.in_game_placement
                FROM game_result gr
                JOIN game g ON gr.game_id = g.id
                WHERE g.num_players = ?
                ORDER BY gr.final_score DESC
                """;

        List<RankingEntry> ranking = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(query)) {
            ps.setInt(1, numPlayers);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ranking.add(new RankingEntry(
                        rs.getString("nickname"),
                        rs.getInt("final_score"),
                        rs.getTimestamp("game_date").toLocalDateTime(),
                        rs.getInt("num_players"),
                        rs.getInt("in_game_placement")
                ));
            }
        }
        return ranking;
    }

    /**
     * Restituisce la posizione globale di un giocatore nella classifica
     * per partite con un dato numero di giocatori (basata sul miglior punteggio).
     */
    public int getPlayerGlobalRank(String nickname, int numPlayers) throws SQLException {
        String query = """
                SELECT COUNT(*) + 1 AS rank_position
                FROM (
                    SELECT gr.nickname, MAX(gr.final_score) AS best_score
                    FROM game_result gr
                    JOIN game g ON gr.game_id = g.id
                    WHERE g.num_players = ?
                    GROUP BY gr.nickname
                ) ranked
                WHERE best_score > (
                    SELECT MAX(gr2.final_score)
                    FROM game_result gr2
                    JOIN game g2 ON gr2.game_id = g2.id
                    WHERE gr2.nickname = ? AND g2.num_players = ?
                )
                """;

        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(query)) {
            ps.setInt(1, numPlayers);
            ps.setString(2, nickname);
            ps.setInt(3, numPlayers);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("rank_position");
        }
    }
}