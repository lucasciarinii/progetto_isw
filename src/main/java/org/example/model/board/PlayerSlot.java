package org.example.model.board;

import org.example.model.match.Player;

import java.util.Objects;

public class PlayerSlot {
    private Player player;
    private final int food;
    private final int points;

    public PlayerSlot(Player player, int food, int points) {
        this.player = player;
        this.food = food;
        this.points = points;
    }

    public String getPlayerName() {
        return player.getNickname();
    }


    public void applyTurnOrderEffect(Player player, int food, int points) {
        this.player = player;

        if ( food < 0 && player.getFood() < 1 ) {
            player.addPoints(points);
            return;
        }

        player.addFood(food);

    }
}