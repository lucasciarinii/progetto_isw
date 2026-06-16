package it.polimi.ingsw.server.model.board.turnOrderTileActions;

import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.exceptions.InvalidCardException;
import it.polimi.ingsw.server.model.exceptions.NoDrawableCardException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.List;

/**
 * Offer action that draws one card from the bottom row.
 */
public class DActionStrategy implements OfferActionStrategy {

    /** Picker for bottom-row cards. */
    private final DPick singleD = new DPick();

    /**
     * Executes the D action.
     *
     * @param match current match
     * @param player acting player
     * @param ids selected card ids
     */
    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // 1) Check if row has drawable cards
        int pickable = (int) countPickable(match.getBoard().getBottomRow(), player);

        // 2) If not, throw exception to skip turn
        if (pickable == 0)
            throw new NoDrawableCardException("No drawable card in the bottom row, turn skipped.");

        // 3) Try to pick the card - if ID is not valid, InvalidCardException -> client will have to choose again
        Card c1 = singleD.execute(match, player, ids.getFirst());

        // No possible exception from here on, so we can add the card to the player and remove it from the board
        applyCard(c1, player, match.getBoard().getBottomRow());

    }
}