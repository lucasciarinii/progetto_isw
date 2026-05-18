package org.example.server.model.board;

import org.example.server.model.match.Player;
import java.util.List;
import java.util.Objects;

/**
 * Turn order tile containing slots with their food/points effects.
 */
public class TurnOrderTile {
    /** Ordered list of player slots. */
    private final List<PlayerSlot> slots;

    /**
     * Creates the turn order tile based on the number of players.
     *
     * @param players list of players in the match
     */
    public TurnOrderTile(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");
        // Random order is already set in Match.init() so we can use progressively the players in the list to assign them to the slots

        // Initialize player slots based on the number of players
        switch (players.size()) {
            case 2 -> slots = List.of(new PlayerSlot(players.get(0), 1, 0), new PlayerSlot(players.get(1), -1, -2));
            case 3 -> slots = List.of(new PlayerSlot(players.get(0), 2, 0), new PlayerSlot(players.get(1), 0, 0), new PlayerSlot(players.get(2), -1, -2));
            case 4 -> slots = List.of(new PlayerSlot(players.get(0), 2, 0), new PlayerSlot(players.get(1), 1, 0), new PlayerSlot(players.get(2), 0, 0), new PlayerSlot(players.get(3), -1, -2));
            case 5 -> slots = List.of(new PlayerSlot(players.get(0), 3, 0), new PlayerSlot(players.get(1), 1, 0), new PlayerSlot(players.get(2), 0, 0), new PlayerSlot(players.get(3), 0, 0), new PlayerSlot(players.get(4), -1, -2));
            default -> throw new IllegalArgumentException("Invalid list of players");
        }
    }

    /**
     * @return the list of player slots
     */
    public List<PlayerSlot> getSlots(){return slots;}
}
