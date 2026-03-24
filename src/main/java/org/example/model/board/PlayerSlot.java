package org.example.model.board;

import org.example.model.match.Player;
import org.jetbrains.annotations.NotNull;

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

        if ( player == null ) {
            return "";
        }

        return player.getNickname();
    }


    public void placePlayerAndApplyEffect(@NotNull Player player, int food, int points) {

        Objects.requireNonNull(player, "player parameter can't be null");

        if ( this.player != null ) {
            throw new IllegalArgumentException("tile already taken");
        }

        this.player = player;

        if ( food < 0 && player.getFood() < 1 ) {
            player.addPoints(points);
            return;
        }

        player.addFood(food);

    }


    public void removeTotem() {
        this.player = null;
    }
}