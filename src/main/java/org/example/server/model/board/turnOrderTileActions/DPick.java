package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DPick {
    public Card execute(Match match, Player player, int id) throws NoDrawableCardException, InvalidCardException {
        Board board = match.getBoard();

        // 2) Find card with corresponding ID
        Card card = board.getBottomRow().stream()
                .filter(c -> c.getId() == id)
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow(() -> new InvalidCardException("Invalid ID card: " + id));

        // 3) Cost handling if BuildingCard
        if (card.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) card;
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost()) {
                throw new InvalidCardException("Player doesn't have enough food to buy the building card with ID: " + id);
            } else {
                return buildingCard;
            }
        }

        return card;
    }
}
