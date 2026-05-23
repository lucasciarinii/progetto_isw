package org.example.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

//? Message sent to all clients in waiting lobby -> It updates them about how many player are connected, and how many more are needed to start the game
public class LobbyUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int connectedPlayers;
    private final int requiredPlayers;
    private final List<String> playerNicknames;
    private final boolean gameStarting;  // true = the game is about to start, false = still waiting for players
    private final String gameID;

    @JsonCreator
    public LobbyUpdateMessage(
            @JsonProperty("connectedPlayers") int connectedPlayers,
            @JsonProperty("requiredPlayers") int requiredPlayers,
            @JsonProperty("playerNicknames") List<String> playerNicknames,
            @JsonProperty("gameStarting") boolean gameStarting,
            @JsonProperty("gameID") String gameID) {
        this.connectedPlayers = connectedPlayers;
        this.requiredPlayers = requiredPlayers;
        this.playerNicknames = List.copyOf(playerNicknames);
        this.gameStarting = gameStarting;
        this.gameID = gameID;
    }

    public int getConnectedPlayers()      { return connectedPlayers; }
    public int getRequiredPlayers()       { return requiredPlayers; }
    public List<String> getPlayerNicknames() { return playerNicknames; }
    public boolean isGameStarting()       { return gameStarting; }
    public String getGameID() {
        return this.gameID;
    }
}
