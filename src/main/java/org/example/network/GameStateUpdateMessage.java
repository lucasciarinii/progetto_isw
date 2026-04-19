package org.example.network;

import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.network.Snapshots.TurnSlotSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.GamePhase;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

// This is the DTO (Data Transfer Object): a serializable object containing everything the view needs to update the game.
// It's useless send all the Match class since it contains a lot of logic and references to other objects. Instead, we create a simple DTO with only the necessary data for the view.

public class GameStateUpdateMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Game flow
    private final int currentRound;
    private final Era currentEra;
    private final GamePhase currentPhase;
    private final String currentPlayerNickname;
    private final List<String> turnOrder; // nicknames in turn order

    // Board
    private final List<Card> topRow;
    private final List<Card> bottomRow;
    private final List<OfferTileSnapshot> offerTrack;
    private final List<TurnSlotSnapshot> turnOrderSlots;

    // Players (public snapshot of each player)
    private final List<PlayerSnapshot> players;

    // Game over
    private final List<String> winners; // nicknames of winners (empty if game not over)

    public GameStateUpdateMessage(
            int currentRound,
            Era currentEra,
            GamePhase currentPhase,
            String currentPlayerNickname,
            List<String> turnOrder,
            List<Card> topRow,
            List<Card> bottomRow,
            List<OfferTileSnapshot> offerTrack,
            List<TurnSlotSnapshot> turnOrderSlots,
            List<PlayerSnapshot> players,
            List<String> winners) {

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

    public int getCurrentRound()                      { return currentRound; }
    public Era getCurrentEra()                        { return currentEra; }
    public GamePhase getCurrentPhase()                { return currentPhase; }
    public String getCurrentPlayerNickname()          { return currentPlayerNickname; }
    public List<String> getTurnOrder()                { return turnOrder; }
    public List<Card> getTopRow()                     { return topRow; }
    public List<Card> getBottomRow()                  { return bottomRow; }
    public List<OfferTileSnapshot> getOfferTrack()    { return offerTrack; }
    public List<TurnSlotSnapshot> getTurnOrderSlots() { return turnOrderSlots; }
    public List<PlayerSnapshot> getPlayers()          { return players; }
    public List<String> getWinners()                  { return winners; }

}
