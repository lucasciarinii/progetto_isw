package org.example.server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameDAO {

    /**
     Save a complete game on a DB
     @param numPlayers number of players in the game
     @param results    maps nickname -> final score
     @param placements maps nickname -> final position (1 = first)
     */

    public void saveGame(int numPlayers, Map<String, Integer> results, Map<String, Integer> placements) throws SQLException {

        Connection conn = DatabaseConnection.getInstance();
        conn.setAutoCommit(false); // we have to do 2 operations, so we want to commit only if both succeed

        try {
            // 1) Insert the game and get the generated ID
            int gameId;
            String insertGame = "INSERT INTO game (num_players) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertGame, Statement.RETURN_GENERATED_KEYS)) { // with Statement.RETURN_GENERATED_KEYS we can get the auto-incremented ID needed for the next step
                ps.setInt(1, numPlayers);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                gameId = keys.getInt(1);
            }

            // 2) Insert results for each player
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
     Returns the ranking of players for the specific number of players of the match,
     ordered by number of wins and then by average score (descending).
     @param numPlayers the number of players in the matches to consider for the ranking
     */
    public List<RankingEntry> getRanking(int numPlayers) throws SQLException {
        String query = """
            SELECT nickname, wins, avg_score
            FROM ranking_view
            WHERE num_players = ?
            ORDER BY rank_position
            """;

        List<RankingEntry> ranking = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(query)) {
            ps.setInt(1, numPlayers);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ranking.add(new RankingEntry(
                        rs.getString("nickname"),
                        rs.getInt("wins"),
                        rs.getDouble("avg_score")
                ));
            }
        }
        return ranking;
    }

    /**
     Returns position of that player in the ranking for matches with a given number of players,
     based on wins and average score.
        @param nickname the player nickname
        @param numPlayers the number of players in the matches to consider for the ranking
     */
    public int getPlayerGlobalRank(String nickname, int numPlayers) throws SQLException {
        String query = """
            SELECT rank_position
            FROM ranking_view
            WHERE num_players = ? AND nickname = ?
            """;

        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(query)) {
            ps.setInt(1, numPlayers);
            ps.setString(2, nickname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank_position");
            }
            return -1; // player not found in the ranking
        }
    }
}