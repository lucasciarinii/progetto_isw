package org.example.model.board;

public class PlayerSlot {
    private String playerName;

    public PlayerSlot(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
