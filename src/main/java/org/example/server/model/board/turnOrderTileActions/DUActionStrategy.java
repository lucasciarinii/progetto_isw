package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DUActionStrategy implements OfferActionStrategy{

    @Override
    public void execute(Match match, Player player, List<Integer> ids) {

        Board board = match.getBoard();

        // 1) The number of selected cards must be 2
        if (ids.size() != 2) {
            throw new IllegalArgumentException("Invalid String: player must select exactly 2 IDs from cards");
        }

        // 2) If the row does not contain any card at all, an exception will be thrown
        if( board.getBottomRow().isEmpty() || board.getTopRow().isEmpty() ) {
            throw new IllegalArgumentException("The row is empty, no card can be selected");
        }

        // 3) Find card with corresponding ID from bottomRow
        Card bottomCard = board.getBottomRow().stream()
                .filter(c -> c.getId() == ids.getFirst())
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

        // 4) Find card with corresponding ID from topRow
        Card topCard = board.getTopRow().stream()
                .filter(c -> c.getId() == ids.get(1))
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

        // 5) Cost handling if BuildingCard
        if (bottomCard.isBuilding() && topCard.isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) bottomCard;
            BuildingCard buildingCard1 = (BuildingCard) topCard;

            // Check if the player has enough food to take both building cards
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            }
            else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding()));
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                board.getBottomRow().remove(buildingCard0);
                board.getTopRow().remove(buildingCard1);
                return;
            }
        }
        if (bottomCard.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) bottomCard;
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            }
            else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getBottomRow().remove(buildingCard);
                player.acceptCard(topCard);
                board.getTopRow().remove(topCard);
                return;
            }
        }
        if (topCard.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) topCard;
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            }
            else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getTopRow().remove(buildingCard);
                player.acceptCard(bottomCard);
                board.getBottomRow().remove(bottomCard);
                return;
            }
        }

        // 6) Add cards to player
        player.acceptCard(bottomCard);
        player.acceptCard(topCard);

        // 7) Remove cards from TopRow
        board.getBottomRow().remove(bottomCard);
        board.getTopRow().remove(topCard);


    }
}
