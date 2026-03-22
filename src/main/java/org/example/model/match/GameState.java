/*
- GameState rappresenta il flusso di gioco: sa in ogni momento in che punto della partita siamo.
- Tiene traccia di round, fasi, turno correnti, ordine di gioco e vincitore.
- Avanza correttamente tra fasi e round
- LA LOGICA DI GIOCO VA NEL CONTROLLER, ma GameState fornisce i metodi per avanzare tra fasi e round.
*/
package org.example.model.match;

import org.example.model.enums.Era;
import org.example.model.enums.GamePhase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private int currentRound;
    private Era currentEra;
    private GamePhase currentPhase;
    private List<Player> turnOrder;
    private int currentPlayerIndex;
    private Player winner;

    public GameState(List<Player> turnOrder) {
        this.currentRound = 1;
        this.currentPhase = GamePhase.PLACE_TOTEMS;
        this.turnOrder = new ArrayList<>(turnOrder);
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    public int getCurrentRound(){return currentRound;}

    public void advanceRound(){currentRound++;}

    public Era getCurrentEra() {
        return currentEra;
    }

    public void advanceCurrentEra() {
        switch (currentEra) {
            case I -> currentEra = Era.II;
            case II -> currentEra = Era.III;
            default -> throw new IllegalStateException("Unexpected value: " + currentEra);
        }
    }

    public GamePhase getCurrentPhase(){return currentPhase;}

    public void advancePhase(){
        currentPhase = currentPhase.next(this);
    }

    public List<Player> getTurnOrder(){
        return Collections.unmodifiableList(turnOrder);
    }

    public void updateTurnOrder(List<Player> newOrder) {
        this.turnOrder = new ArrayList<>(newOrder);
        this.currentPlayerIndex = 0;
    }

    public Player getCurrentPlayer(){return turnOrder.get(currentPlayerIndex);}

    public void advanceToNextPlayer() {
        if (currentPlayerIndex==turnOrder.size()-1) {
            currentPlayerIndex=0;
            advancePhase();
        }
        else currentPlayerIndex++;
    }

    public boolean isGameOver() {
        return currentPhase == GamePhase.GAME_OVER;
    }

    public Player getWinner(){return winner;}

    public void setWinner(Player player){
        this.winner = player;
        this.currentPhase = GamePhase.GAME_OVER;
    }

}
