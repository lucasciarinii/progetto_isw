package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DDActionStrategy implements OfferActionStrategy {

    @Override
    public void execute(Match match, Player player, List<Integer> id) {

        Board board = match.getBoard();

        // 1) The number of selected cards must be 2
        if (id.size() != 2) {
            throw new IllegalArgumentException("Invalid String: player must select exactly 2 IDs from cards");
        }

        // 2) If the row does not contain any card at all, an exception will be thrown
        if (board.getBottomRow().isEmpty()) {
            throw new IllegalArgumentException("The row is empty, no card can be selected");
        }

        // 3) Find card with corresponding IDs
        List<Card> cardsInput = board.getBottomRow().stream()
                .filter(c -> (c.getId() == id.getFirst() || c.getId() == id.get(1)))
                .filter(c -> c.isCharacter() || c.isBuilding())
                .toList();

        // 4) If cardsInput.size() != 2 means that at least one of the two selected IDs is invalid (not present in the bottom row or not a Character card)
        if (cardsInput.size() != 2) {
            throw new IllegalArgumentException("Invalid ID cards");
        }

        // 5) Cost handling if BuildingCard
        if (cardsInput.get(0).isBuilding() && cardsInput.get(1).isBuilding()) {
            BuildingCard buildingCard0 = (BuildingCard) cardsInput.get(0);
            BuildingCard buildingCard1 = (BuildingCard) cardsInput.get(1);

            // Check if the player has enough food to take both building cards
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost()) {
                throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
            } else {
                player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding()));
                player.acceptCard(buildingCard0);
                player.acceptCard(buildingCard1);
                board.getBottomRow().remove(buildingCard0);
                board.getBottomRow().remove(buildingCard1);
                return;
            }
        }
        if (cardsInput.get(0).isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) cardsInput.get(0);
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost()) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            } else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getBottomRow().remove(buildingCard);
                player.acceptCard(cardsInput.get(1));
                board.getBottomRow().remove(cardsInput.get(1));
                return;
            }
        }
        if (cardsInput.get(1).isBuilding()) {
            BuildingCard buildingCard = (BuildingCard) cardsInput.get(1);
            if (player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost()) {
                throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
            } else {
                player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                player.acceptCard(buildingCard);
                board.getBottomRow().remove(buildingCard);
                player.acceptCard(cardsInput.getFirst());
                board.getBottomRow().remove(cardsInput.getFirst());
                return;
            }
        }

        // 6) Add cards to player
        player.acceptCard(cardsInput.get(0));
        player.acceptCard(cardsInput.get(1));

        // 7) Remove cards from bottomRow
        board.getBottomRow().remove(cardsInput.get(0));
        board.getBottomRow().remove(cardsInput.get(1));
    }
}
