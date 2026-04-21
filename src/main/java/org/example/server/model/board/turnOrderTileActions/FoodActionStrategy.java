package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class FoodActionStrategy implements OfferActionStrategy {

    private static final int FOOD_BONUS = 3;

    // When the player chooses the tile with the food bonus, they receive the bonus.
    @Override
    public void execute(Match match, Player player, int id) {
        if ( id > 0 ) {
            throw new IllegalArgumentException("FOOD effect does not accept card ID");
        }

        player.addFood(FOOD_BONUS);
    }
}
