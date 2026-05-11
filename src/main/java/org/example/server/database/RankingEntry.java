package org.example.server.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

// DTO used in RankingUpdateMessage: must be Serializable to be sent over RMI
public class RankingEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nickname;
    private final int wins;
    private final double avgScore;

    @JsonCreator
    public RankingEntry(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("wins") int wins,
            @JsonProperty("avgScore") double avgScore) {
        this.nickname = nickname;
        this.wins = wins;
        this.avgScore = avgScore;
    }

    public String getNickname() { return nickname; }
    public int getWins()        { return wins; }
    public double getAvgScore() { return avgScore; }

    @Override
    public String toString() {
        return String.format("%s | Wins: %d | Avg score: %.1f", nickname, wins, avgScore);
    }
}