package org.example.server.database;

import java.time.LocalDateTime;

public class RankingEntry {
    private final String nickname;
    private final int wins;
    private final double avgScore;

    public RankingEntry(String nickname, int wins, double avgScore) {
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