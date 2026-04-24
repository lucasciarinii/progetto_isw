package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class UActionStrategy implements OfferActionStrategy {

    private final UPick singleU = new UPick();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // Try to pick the card
        Card c1 = singleU.execute(match, player, ids.getFirst());

        // If no exception is thrown, then we can add the cards to the player and remove them from the board
        if (c1.isBuilding()) {
            player.addFood(Math.min(0, -((BuildingCard) c1).getFoodCost() + player.getDiscountOnBuilding()));
        }
        player.acceptCard(c1);
        match.getBoard().getTopRow().remove(c1);
    }
}