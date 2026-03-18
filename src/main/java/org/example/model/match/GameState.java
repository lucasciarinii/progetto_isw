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

    public Player getCurrentPlayer(){return turnOrder.get(currentPlayerIndex);}

    public List<Player> getTurnOrder(){return turnOrder;}

    public Player getWinner(){return winner;}

    // TODO
    public boolean isGameOver(){return false;}

    public void advanceToNextPlayer(){
        if (currentPlayerIndex==turnOrder.size()-1) {
            currentPlayerIndex=0;
            advancePhase();
        }
        else currentPlayerIndex++;
    }

    // TODO
    public void advancePhase(){}

    public void updateTurnOrder(List<Player> newOrder){turnOrder=newOrder;}

    public void setWinner(Player player){winner=player;}

}
