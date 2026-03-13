package org.example.model.match;

import org.example.model.enums.GamePhase;

import java.util.List;

public class GameState {
    private int currentRound;
    private GamePhase currentPhase;
    private List<Player> turnOrder;
    private int currentPlayerIndex;
    private Player winner;

    public GameState() {
    }

    public int getCurrentRound(){return currentRound;}

    public GamePhase getCurrentPhase(){return currentPhase;}

    public Player getCurrentPlayer(){return turnOrder[currentPlayerIndex];}

    public List<Player> getTurnOrder(){return turnOrder;}

    public Player getWinner(){return winner;}

    public boolean isGameOver(){}

    public void advanceToNextPlayer(){
        if (currentPlayerIndex==turnOrder.length()-1) {
            currentPlayerIndex=0;
            advancePhase();
        }
        else currentPlayerIndex++;
    }

    public void advancePhase(){}

    public void updateTurnOrder(List<Player> newOrder){turnOrder=newOrder;}

    public void setWinner(Player player){winner=player;}

}
