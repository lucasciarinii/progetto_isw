package org.example.model.board;

import org.example.model.match.Player;

import java.util.Objects;

public class PlayerSlot {
    private Player player;
    private final int food;
    private final int points;

    public PlayerSlot(Player player, int food, int points) {
        if (points > 0) {
            throw new IllegalArgumentException("points must be negative or equal to zero");
        }
        this.player = player;
        this.food = food;
        this.points = points;
    }

    public Player getPlayer() {
        return player;
    }


    public int getFood() {
        return food;
    }

    public int getPoints() {
        return points;
    }

    // Places the player on the slot and triggers the effect of it.
    // used during the game phases when the player
    public void placePlayerAndApplyEffect(Player player) {

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
