package it.polimi.ingsw.server.model.board.turnOrderTileActions;

import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.exceptions.InvalidCardException;
import it.polimi.ingsw.server.model.exceptions.NoDrawableCardException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.List;

/**
 * Strategy-Pattern interface for resolving offer track actions.
 */
public interface OfferActionStrategy {
    /**
     * Executes the action for the given player and selected card ids.
     *
     * @param match current match
     * @param player acting player
     * @param ids selected card ids
     * @throws NoDrawableCardException when no cards can be drawn
     * @throws InvalidCardException when selected ids are invalid
     */
    void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException;

    /**
     * Convenience overload for single-card actions.
     *
     * @param match current match
     * @param player acting player
     * @param id selected card id
     */
    default void execute(Match match, Player player, int id) throws NoDrawableCardException, InvalidCardException {
        execute(match, player, List.of(id));
    }

    /**
     * Counts drawable cards for the player in the given row.
     *
     * @param row card row
     * @param player acting player
     * @return number of drawable cards
     */
    default long countPickable(List<Card> row, Player player) {
        return row.stream()
                .filter(c -> c.isCharacter() ||
                        (c.isBuilding() &&
                                ((BuildingCard) c).getFoodCost() <= player.getFood()
                                        + player.getDiscountOnBuilding()))
                .count();
    }

    /**
     * Applies the card to the player and removes it from the row.
     *
     * @param c selected card
     * @param player acting player
     * @param row card row
     */
    default void applyCard(Card c, Player player, List<Card> row) {
        if (c.isBuilding()) {
            player.addFood(Math.min(0, -((BuildingCard) c).getFoodCost()
                    + player.getDiscountOnBuilding()));
        }
        player.acceptCard(c);
        row.remove(c);
    }
}
