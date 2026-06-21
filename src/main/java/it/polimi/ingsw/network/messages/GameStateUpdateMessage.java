package it.polimi.ingsw.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.network.snapshots.OfferTileSnapshot;
import it.polimi.ingsw.network.snapshots.PlayerSnapshot;
import it.polimi.ingsw.network.snapshots.TurnSlotSnapshot;
import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO carrying a full snapshot of the match state for client views.
 */
@SuppressWarnings("ClassCanBeRecord")
public class GameStateUpdateMessage implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final int currentRound;
    private final Era currentEra;
    private final GamePhase currentPhase;
    private final String currentPlayerNickname;

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
