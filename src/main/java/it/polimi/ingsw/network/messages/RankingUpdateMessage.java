package it.polimi.ingsw.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.database.RankingEntry;

import java.io.Serializable;
import java.util.List;

/**
 * DTO carrying the global ranking list and the recipient's position.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RankingUpdateMessage implements Serializable {

    private final List<RankingEntry> ranking;
    private final int playerRankPosition;

    @JsonCreator
    public RankingUpdateMessage(
            @JsonProperty("ranking") List<RankingEntry> ranking,
            @JsonProperty("playerRankPosition") int playerRankPosition) {
        this.ranking = ranking;
        this.playerRankPosition = playerRankPosition;
    }

    /**
     * Returns the ordered ranking list.
     *
     * @return the ranking entries
     */
    public List<RankingEntry> getRanking()       { return ranking; }

    /**
     * Returns the recipient's rank position, or -1 if not ranked.
     *
     * @return the player's rank position
     */
    public int getPlayerRankPosition()           { return playerRankPosition; }
}
