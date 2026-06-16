package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Tracks the match flow and exposes transition helpers for rounds, phases, and turn order.
 * Game rules are enforced by the controller; this class only advances state.
 */
public class GameState {
    private int currentRound;
    private Era currentEra;
    private GamePhase currentPhase;
    private List<Player> turnOrder;
    private int currentPlayerIndex;
    private List<Player> winners = new ArrayList<>();

    public GameState(List<Player> turnOrder) {
        Objects.requireNonNull(turnOrder, "Turn order cannot be null");
        this.currentRound = 1;
        this.currentEra = Era.I;
        this.currentPhase = GamePhase.PLACE_TOTEMS;
        this.turnOrder = new ArrayList<>(turnOrder);
        this.currentPlayerIndex = 0;
    }

    public int getCurrentRound(){return currentRound;}

    /**
    * Advances to the next round (incrementing just the round number)
    */
    public void advanceRound(){currentRound++;}

    public Era getCurrentEra() {
        return currentEra;
    }

    /**
     * Advances to the next era
     */
    public void advanceCurrentEra() {
        switch (currentEra) {
            case I -> currentEra = Era.II;
            case II -> currentEra = Era.III;
            default -> throw new IllegalStateException("Unexpected value: " + currentEra);
        }
    }

    public GamePhase getCurrentPhase(){return currentPhase;}

    /**
     * Advances the game to the next phase using the phase transition rules.
     */
    public void advancePhase(){
        currentPhase = currentPhase.next(this);
    }

    public List<Player> getTurnOrder(){
        return Collections.unmodifiableList(turnOrder);
    }

    /**
     * Replaces the turn order and resets the current player index.
     */
    public void updateTurnOrder(List<Player> newOrder) {
        this.turnOrder = new ArrayList<>(newOrder);
        this.currentPlayerIndex = 0;
    }

    public Player getCurrentPlayer(){return turnOrder.get(currentPlayerIndex);}

    /**
     * Sets the current player to the given one, if present in the turn order.
     */
    public void setCurrentPlayer(Player player) {
        Objects.requireNonNull(player, "player cannot be null");
        int idx = turnOrder.indexOf(player);
        if (idx < 0) {
            throw new IllegalArgumentException("Player not found in turn order: " + player.getNickname());
        }
        currentPlayerIndex = idx;
    }

    /**
     * Advances to the next player; when the order ends, resets and advances the phase.
     */
    public void advanceToNextPlayer() {
        if (currentPlayerIndex==turnOrder.size()-1) {
            currentPlayerIndex=0;
            advancePhase();
        }
        else currentPlayerIndex++;
    }

    /**
     * Checks if the current phase of the game is over.
     *
     * @return {@code true} if the current phase is GAME_OVER; {@code false} otherwise
     */
    public boolean isGameOver() {
        return currentPhase == GamePhase.GAME_OVER;
    }

    /**
     * Returns the list of winners, which is empty until the game ends.
     */
    public List<Player> getWinners(){return List.copyOf(winners);}

    /**
     * Sets the list of winners. It is only called once, when the game ends.
     *
     * @param players the list of winners to set
     */
    public void setWinners(List<Player> players){
        this.winners = players;
        this.currentPhase = GamePhase.GAME_OVER;
    }

}
