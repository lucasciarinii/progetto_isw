package org.example.network;

import org.example.server.database.RankingEntry;

import java.io.Serializable;
import java.util.List;

public class RankingUpdateMessage implements Serializable {

    private final List<RankingEntry> ranking;
    private final int playerRankPosition; // -1 se non in classifica

    public RankingUpdateMessage(List<RankingEntry> ranking, int playerRankPosition) {
        this.ranking = ranking;
        this.playerRankPosition = playerRankPosition;
    }

    public List<RankingEntry> getRanking()       { return ranking; }
    public int getPlayerRankPosition()           { return playerRankPosition; }
}
