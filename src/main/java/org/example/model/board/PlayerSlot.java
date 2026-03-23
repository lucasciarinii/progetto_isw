package org.example.model.board;

import java.util.Objects;

public class PlayerSlot {
    private String playerName;

    public PlayerSlot(String playerName) {
        this.playerName = Objects.requireNonNull(playerName, "Player name cannot be null");
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
