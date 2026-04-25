package org.example.server.database;

import java.time.LocalDateTime;

public class RankingEntry {
    private final String nickname;
    private final int finalScore;
    private final LocalDateTime gameDate;
    private final int numPlayers;
    private final int inGamePlacement;

    public RankingEntry(String nickname, int finalScore, LocalDateTime gameDate,
                        int numPlayers, int inGamePlacement) {
        this.nickname = nickname;
        this.finalScore = finalScore;
        this.gameDate = gameDate;
        this.numPlayers = numPlayers;
        this.inGamePlacement = inGamePlacement;
    }

    public String getNickname()       { return nickname; }
    public int getFinalScore()        { return finalScore; }
    public LocalDateTime getGameDate(){ return gameDate; }
    public int getNumPlayers()        { return numPlayers; }
    public int getInGamePlacement()   { return inGamePlacement; }

    @Override
    public String toString() {
        return nickname + " | Score: " + finalScore + " | Date: " + gameDate + " | Placement: " + inGamePlacement;
    }
}