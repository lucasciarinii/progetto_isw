package org.example.server.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Ranking entry DTO used in ranking updates.
 */
// DTO used in RankingUpdateMessage: must be Serializable to be sent over RMI
public class RankingEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Player nickname. */
    private final String nickname;
    /** Number of wins. */
    private final int wins;
    /** Average score across games. */
    private final double avgScore;

    /**
     * Creates a ranking entry.
     *
     * @param nickname player nickname
     * @param wins number of wins
     * @param avgScore average score
     */
    @JsonCreator
    public RankingEntry(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("wins") int wins,
            @JsonProperty("avgScore") double avgScore) {
        this.nickname = nickname;
        this.wins = wins;
        this.avgScore = avgScore;
    }

    /**
     * @return player nickname
     */
    public String getNickname() { return nickname; }

    /**
     * @return number of wins
     */
    public int getWins()        { return wins; }

    /**
     * @return average score
     */
    public double getAvgScore() { return avgScore; }

    @Override
    public String toString() {
        return String.format("%s | Wins: %d | Avg score: %.1f", nickname, wins, avgScore);
    }
}