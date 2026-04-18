package org.example.network;

import java.io.Serializable;
import java.util.List;

//? Message sent to all clients in waiting lobby -> It updates them about how many player are connected, and how many more are needed to start the game
public class LobbyUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int connectedPlayers;
    private final int requiredPlayers;
    private final List<String> playerNicknames;
    private final boolean gameStarting;  // true = the game is about to start, false = still waiting for players

    public LobbyUpdateMessage(int connectedPlayers, int requiredPlayers, List<String> playerNicknames, boolean gameStarting) {
        this.connectedPlayers = connectedPlayers;
        this.requiredPlayers = requiredPlayers;
        this.playerNicknames = List.copyOf(playerNicknames);
        this.gameStarting = gameStarting;
    }

    public int getConnectedPlayers()      { return connectedPlayers; }
    public int getRequiredPlayers()       { return requiredPlayers; }
    public List<String> getPlayerNicknames() { return playerNicknames; }
    public boolean isGameStarting()       { return gameStarting; }
}
