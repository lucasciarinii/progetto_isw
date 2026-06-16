package it.polimi.ingsw.server.model.board.turnOrderTileActions;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.List;

/**
 * Offer action that grants a flat food bonus.
 */
public class FoodActionStrategy implements OfferActionStrategy {

    /** Food bonus granted by the tile. */
    private static final int FOOD_BONUS = 3;

    /**
     * Executes the FOOD action.
     *
     * @param match current match
     * @param player acting player
     * @param ids selected card ids (must be empty)
     */
    // When the player chooses the tile with the food bonus, they receive the bonus.
    @Override
    public void execute(Match match, Player player, List<Integer> ids) {
        if (!ids.isEmpty()) {
            throw new IllegalArgumentException("FOOD effect does not accept card IDs");
        }

        player.addFood(FOOD_BONUS);
    }
}
