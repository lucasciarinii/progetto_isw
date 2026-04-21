package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DUUActionStrategy implements OfferActionStrategy{
    
    @Override
    public void execute(Match match, Player player, List<Integer> id) {

        Board board = match.getBoard();

        // 1) The number of selected cards must be 3
        if (id.size() != 3) {
            throw new IllegalArgumentException("Invalid String: player must select exactly 3 IDs from cards");
        }

        // 2) If the row does not contain any card at all, an exception will be thrown
        if( board.getBottomRow().isEmpty() || board.getTopRow().isEmpty() ) {
            throw new IllegalArgumentException("The row is empty, no card can be selected");
        }

        // 3) Find the card with corresponding ID from bottomRow
        Card bottomCard = board.getBottomRow().stream()
                .filter(c -> c.getId() == id.getFirst())
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

        Card topCard1 = board.getTopRow().stream()
                .filter(c -> c.getId() == id.get(1))
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

        // 4) Find the card with corresponding ID from topRow
        Card topCard2 = board.getTopRow().stream()
                .filter(c -> c.getId() == id.get(2))
                .filter(c -> c.isCharacter() || c.isBuilding())
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

        // 5) Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
        if (topCard1.isBuilding() && topCard2.isBuilding() && bottomCard.isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) topCard1;
            BuildingCard buildingCard1 = (BuildingCard) topCard2;
            BuildingCard buildingCard2 = (BuildingCard) bottomCard;

            // Check if the player has enough food to take both building cards
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() + buildingCard2.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            }
            else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() - buildingCard2.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                player.acceptCard(buildingCard2);
                board.getTopRow().remove(buildingCard0);
                board.getTopRow().remove(buildingCard1);
                board.getBottomRow().remove(buildingCard2);
                return;
            }
        }
        if (bottomCard.isBuilding() && topCard1.isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) bottomCard;
            BuildingCard buildingCard1 = (BuildingCard) topCard1;

            // Check if the player has enough food to take both building cards
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            }
            else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                board.getBottomRow().remove(buildingCard0);
                board.getTopRow().remove(buildingCard1);
                player.acceptCard(topCard2);
                board.getTopRow().remove(topCard2);
                return;
            }
        }
        if (bottomCard.isBuilding() && topCard2.isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) bottomCard;
            BuildingCard buildingCard1 = (BuildingCard) topCard2;

            // Check if the player has enough food to take both building cards
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            }
            else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                board.getBottomRow().remove(buildingCard0);
                board.getTopRow().remove(buildingCard1);
                player.acceptCard(topCard1);
                board.getTopRow().remove(topCard1);
                return;
            }
        }
        if (topCard1.isBuilding() && topCard2.isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) topCard1;
            BuildingCard buildingCard1 = (BuildingCard) topCard2;

            // Check if the player has enough food to take both building cards
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            }
            else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                board.getTopRow().remove(buildingCard0);
                board.getTopRow().remove(buildingCard1);
                player.acceptCard(bottomCard);
                board.getBottomRow().remove(bottomCard);
                return;
            }
        }
        if (topCard1.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) topCard1;
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            }
            else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getTopRow().remove(buildingCard);
                player.acceptCard(topCard2);
                board.getTopRow().remove(topCard2);
                player.acceptCard(bottomCard);
                board.getBottomRow().remove(bottomCard);
                return;
            }
        }
        if (topCard2.isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) topCard2;
            if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            }
            else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getTopRow().remove(buildingCard);
                player.acceptCard(topCard1);
                board.getTopRow().remove(topCard1);
                player.acceptCard(bottomCard);
                board.getBottomRow().remove(bottomCard);
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
                player.acceptCard(topCard1);
                board.getTopRow().remove(topCard1);
                player.acceptCard(topCard2);
                board.getTopRow().remove(topCard2);
            }
        }
    }
}
