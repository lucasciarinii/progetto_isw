package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;


public class DActionStrategy implements OfferActionStrategy {


    @Override
    public void execute(Match match, Player player, int id) {

        Board board = match.getBoard();

        //TODO: cambiare in modo tale che peschi solamente se le carte sono del tipo "pescabile"
        // 1) If the row does not contain any card at all, an exception will be thrown
        if ( match.getBoard().getBottomRow().isEmpty() ) {
            throw new IllegalArgumentException("The row is empty, no card can be selected");
        }

        // 2) Find card with corresponding ID
        Card card = board.getBottomRow().stream()
                .filter(c -> c.getId() == id)
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("Invalid ID card") );

        // 3) Cost handling if BuildingCard
        if (card.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) card;
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost()) {
                throw new IllegalArgumentException("Player doesn't have enough food to buy this building card");
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
