package org.example.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.network.snapshots.TurnSlotSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.GamePhase;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO carrying a full snapshot of the match state for client views.
 */
public class GameStateUpdateMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int currentRound;
    private final Era currentEra;
    private final GamePhase currentPhase;
    private final String currentPlayerNickname;
    private final List<String> turnOrder;

    private final List<Card> topRow;
    private final List<Card> bottomRow;
    private final List<OfferTileSnapshot> offerTrack;
    private final List<TurnSlotSnapshot> turnOrderSlots;

    private final List<PlayerSnapshot> players;

    private final List<String> winners;

    @JsonCreator
    public GameStateUpdateMessage(
            @JsonProperty("currentRound") int currentRound,
            @JsonProperty("currentEra") Era currentEra,
            @JsonProperty("currentPhase") GamePhase currentPhase,
            @JsonProperty("currentPlayerNickname") String currentPlayerNickname,
            @JsonProperty("turnOrder") List<String> turnOrder,
            @JsonProperty("topRow") List<Card> topRow,
            @JsonProperty("bottomRow") List<Card> bottomRow,
            @JsonProperty("offerTrack") List<OfferTileSnapshot> offerTrack,
            @JsonProperty("turnOrderSlots") List<TurnSlotSnapshot> turnOrderSlots,
            @JsonProperty("players") List<PlayerSnapshot> players,
            @JsonProperty("winners") List<String> winners) {

        this.currentRound = currentRound;
        this.currentEra = currentEra;
        this.currentPhase = currentPhase;
        this.currentPlayerNickname = currentPlayerNickname;
        this.turnOrder = List.copyOf(turnOrder);
        this.topRow = List.copyOf(topRow);
        this.bottomRow = List.copyOf(bottomRow);
        this.offerTrack = List.copyOf(offerTrack);
        this.turnOrderSlots = List.copyOf(turnOrderSlots);
        this.players = List.copyOf(players);
        this.winners = List.copyOf(winners);
    }

    /** @return the current round number */
    public int getCurrentRound()                      { return currentRound; }
    /** @return the current era */
    public Era getCurrentEra()                        { return currentEra; }
    /** @return the current game phase */
    public GamePhase getCurrentPhase()                { return currentPhase; }
    /** @return the nickname of the current player */
    public String getCurrentPlayerNickname()          { return currentPlayerNickname; }
    /** @return the ordered nicknames for the turn sequence */
    public List<String> getTurnOrder()                { return turnOrder; }
    /** @return the cards in the top row */
    public List<Card> getTopRow()                     { return topRow; }
    /** @return the cards in the bottom row */
    public List<Card> getBottomRow()                  { return bottomRow; }
    /** @return the offer track snapshot */
    public List<OfferTileSnapshot> getOfferTrack()    { return offerTrack; }
    /** @return the turn order tile snapshot */
    public List<TurnSlotSnapshot> getTurnOrderSlots() { return turnOrderSlots; }
    /** @return the list of player snapshots */
    public List<PlayerSnapshot> getPlayers()          { return players; }
    /** @return the winners' nicknames, empty if the game is not over */
    public List<String> getWinners()                  { return winners; }

}
