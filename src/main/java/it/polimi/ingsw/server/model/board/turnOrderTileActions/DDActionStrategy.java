package it.polimi.ingsw.server.model.board.turnOrderTileActions;

import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.exceptions.InvalidCardException;
import it.polimi.ingsw.server.model.exceptions.NoDrawableCardException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.List;

/**
 * Offer action that draws up to two cards from the bottom row.
 */
public class DDActionStrategy implements OfferActionStrategy {

    /** Picker for bottom-row cards. */
    private final DPick singleD = new DPick();

    /**
     * Executes the DD action.
     *
     * @param match current match
     * @param player acting player
     * @param ids selected card ids
     */
    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // Count how many drawable cards are currently available in the bottom row.
        int toPick = (int) Math.min(2, countPickable(match.getBoard().getBottomRow(), player));

        // If nothing can be drawn, the action must be skipped.
        if (toPick == 0) {
            throw new NoDrawableCardException("No drawable card in the bottom row, turn skipped.");
        }

        // Validate the number of provided IDs before accessing the list.
        if (ids.size() < toPick) {
            throw new InvalidCardException("Not enough card IDs provided for DD action.");
        }

        // Resolve the selected cards only after input validation.
        Card c1 = singleD.execute(match, player, ids.get(0));
        Card c2 = (toPick == 2) ? singleD.execute(match, player, (int) ids.get(1)) : null;

        // Apply the cards and remove them from the board.
        applyCard(c1, player, match.getBoard().getBottomRow());
        if (c2 != null) {
            applyCard(c2, player, match.getBoard().getBottomRow());
        }
    }
}