package org.example.server.model.board.turnOrderTileActios;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class FoodActionStrategy implements OfferActionStrategy {

    private static final int FOOD_BONUS = 3;

    // When the player chooses the tile with the food bonus, they receive the bonus.
    @Override
    public void execute(Match match, Player player, List<Integer> ids) {
        if ( !ids.isEmpty() ) {
            throw new IllegalArgumentException("FOOD effect does not accept card IDs");
        }

        player.addFood(FOOD_BONUS);
    }
}
