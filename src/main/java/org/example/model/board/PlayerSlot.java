package org.example.model.board;

import org.example.model.match.Player;

import java.util.Objects;

public class PlayerSlot {
    private String playerName;
    private final int food;
    private final int points;

    public PlayerSlot(Player player, int food, int points) {
        this.playerName = Objects.requireNonNull(player.getNickname(), "Player name cannot be null");
        this.food = food;
        this.points = points;
    }

    public String getPlayerName() {
        return playerName;
    }


    public void applyTurnOrderEffect(Player player, int food, int points) {
        this.playerName = player.getNickname();

        if ( food < 0 && player.getFood() < 1 ) {
            player.addPoints(points);
            return;
        }

        player.addFood(food);

    }
}