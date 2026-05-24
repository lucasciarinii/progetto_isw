package org.example.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * DTO sent to lobby clients with the current waiting room status.
 */
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

    /**
     * Returns the number of connected players.
     *
     * @return the connected players count
     */
    public int getConnectedPlayers()      { return connectedPlayers; }

    /**
     * Returns the required number of players to start the match.
     *
     * @return the required players count
     */
    public int getRequiredPlayers()       { return requiredPlayers; }

    /**
     * Returns the ordered list of nicknames in the lobby.
     *
     * @return the player nicknames
     */
    public List<String> getPlayerNicknames() { return playerNicknames; }

    /**
     * Indicates whether the game is starting.
     *
     * @return true if the game is starting
     */
    public boolean isGameStarting()       { return gameStarting; }
    public String getGameID() {
        return this.gameID;
    }
}
