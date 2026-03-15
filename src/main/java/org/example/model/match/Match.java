package org.example.model.match;

import org.example.model.board.Board;

import java.util.List;

public class Match {
    private final int expectedPlayers;

    private List<Player> players;

    private Board board;

    private GameState gameState;

    public Match(int expPl) {
        this.expectedPlayers = expPl;
    }

    public int getExpectedPlayers(){return expectedPlayers;}

    public Board getBoard(){return board;}

    public GameState getGameState(){return gameState;}

    public void init(){}

    public boolean isStarted(){return false; }

    public boolean isOver(){ return false; }

    public boolean isReady(){
        return players.size() == expectedPlayers;
    }

    public void addPlayer(Player p){players.add(p);}

    public List<Player> getPlayers(){return players;}

}
