package it.polimi.ingsw.server.model.board;

import it.polimi.ingsw.server.model.match.Player;

import java.util.Objects;

/**
 * Slot on the turn order tile that holds a player and its reward/penalty.
 */
public class PlayerSlot {
    /** Player currently in the slot, or null if empty. */
    private Player player;
    /** Food delta applied when the slot is taken. */
    private final int food;
    /** Points delta applied when the slot is taken. */
    private final int points;

    /**
     * Creates a player slot with its food and points effects.
     *
     * @param player initial player in the slot
     * @param food food delta
     * @param points points delta (must be <= 0)
     */
    public PlayerSlot(Player player, int food, int points) {
        if (points > 0) {
            throw new IllegalArgumentException("points must be negative or equal to zero");
        }
        this.player = player;
        this.food = food;
        this.points = points;
    }

    /**
     * @return the player occupying the slot, or null
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * @return food delta associated with the slot
     */
    public int getFood() {
        return food;
    }

    /**
     * @return points delta associated with the slot
     */
    public int getPoints() {
        return points;
    }

    /**
     * Places the player on the slot and applies its effect.
     *
     * @param player player to place
     */
    public void placePlayerAndApplyEffect(Player player) {

        Objects.requireNonNull(player, "player parameter can't be null");

        if ( this.player != null ) {
            throw new IllegalArgumentException("tile already taken");
        }

        // set the player to this specific slot
        this.player = player;

        // If we are in the malus slot (food < 0) and the player doesn't have enough food, remove -2 from his points.
        if ( food < 0 && player.getFood() < 1 ) {
            player.addPoints(points);
            return;
        }

        // otherwise, add the food to the player (handle both positive and negative food)
        player.addFood(food);

    }


    /**
     * Removes the player from the slot.
     */
    public void removeTotem() {
        this.player = null;
    }
}
