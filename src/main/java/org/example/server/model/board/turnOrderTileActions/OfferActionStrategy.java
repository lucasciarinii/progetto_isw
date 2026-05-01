package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public interface OfferActionStrategy {
    void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException;

    default void execute(Match match, Player player, int id) throws NoDrawableCardException, InvalidCardException {
        execute(match, player, List.of(id));
    }

    default long countPickable(List<Card> row, Player player) {
        return row.stream()
                .filter(c -> c.isCharacter() ||
                        (c.isBuilding() &&
                                ((BuildingCard) c).getFoodCost() <= player.getFood()
                                        + player.getDiscountOnBuilding()))
                .count();
    }

    default void applyCard(Card c, Player player, List<Card> row) {
        if (c.isBuilding()) {
            player.addFood(Math.min(0, -((BuildingCard) c).getFoodCost()
                    + player.getDiscountOnBuilding()));
        }
        player.acceptCard(c);
        row.remove(c);
    }
}
