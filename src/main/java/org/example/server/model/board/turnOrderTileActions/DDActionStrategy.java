package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DDActionStrategy implements OfferActionStrategy {

    private final DPick singleD = new DPick();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // Try to pick the cards
        Card c1 = singleD.execute(match, player, ids.get(0));
        Card c2 = singleD.execute(match, player, ids.get(1));

        // If no exception is thrown, then we can add the cards to the player and remove them from the board
        if(c1.isBuilding()) {
            player.addFood(Math.min(0, -((BuildingCard) c1).getFoodCost() + player.getDiscountOnBuilding()));
        }
        player.acceptCard(c1);
        match.getBoard().getBottomRow().remove(c1);

        if(c2.isBuilding()) {
            player.addFood(Math.min(0, -((BuildingCard) c2).getFoodCost() + player.getDiscountOnBuilding()));
        }
        player.acceptCard(c2);
        match.getBoard().getBottomRow().remove(c2);

    }
}
