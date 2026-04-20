package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DActionStrategy implements OfferActionStrategy {


    @Override
    public void execute(Match match, Player player, List<Integer> ids) {

        Board board = match.getBoard();


        // 1) The number of selected cards must be 1
        if (ids.size() != 1) {
            throw new IllegalArgumentException("Invalid String: player must select only 1 card");
        }

        // 2) If the row does not contain any card at all, an exception will be thrown
        if ( match.getBoard().getBottomRow().isEmpty() ) {
            throw new IllegalArgumentException("The row is empty, no card can be selected");
        }

        // 3) Find card with corresponding ID
        Card card = board.getBottomRow().stream()
                .filter(c -> c.getId() == ids.getFirst())
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("Invalid ID card") );

        // 4) Cost handling if BuildingCard
        if (card.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) card;
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost()) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            } else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding()));
                player.acceptCard(buildingCard);
                board.getBottomRow().remove(buildingCard);
            }
        }

        // 5) Add card to player
        player.acceptCard(card);

        // 6) Remove card from bottomRow
        board.getBottomRow().remove(card);
    }
}
