package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

/**
 * Offer action that draws one bottom-row card and up to two top-row cards.
 */
public class DUUActionStrategy implements OfferActionStrategy {

    /** Picker for top-row cards. */
    private final UPick singleU = new UPick();
    /** Picker for bottom-row cards. */
    private final DPick singleD = new DPick();

    /**
     * Executes the DUU action.
     *
     * @param match current match
     * @param player acting player
     * @param ids selected card ids
     */
    @Override
    public void execute(Match match, Player player, List ids) throws NoDrawableCardException, InvalidCardException {
        List bottomRow = match.getBoard().getBottomRow();
        List topRow = match.getBoard().getTopRow();

        // Count how many drawable cards are currently available in both rows.
        int pickableBottom = (int) countPickable(bottomRow, player);
        int pickableTop = (int) countPickable(topRow, player);

        // If nothing can be drawn, the action must be skipped.
        if (pickableBottom == 0 && pickableTop == 0) {
            throw new NoDrawableCardException("No drawable cards (both in top and bottom row), turn skipped.");
        }

        int toPickBottom = Math.min(1, pickableBottom);
        int toPickTop = Math.min(2, pickableTop);
        int requiredIds = toPickBottom + toPickTop;

        // Validate the number of provided IDs before accessing the list.
        if (ids.size() < requiredIds) {
            throw new InvalidCardException("Not enough card IDs provided for DUU action.");
        }

        // Resolve the selected cards only after input validation.
        Card cBottom = (toPickBottom == 1) ? singleD.execute(match, player, (int) ids.get(0)) : null;
        Card cTop1 = (toPickTop >= 1) ? singleU.execute(match, player, (int) ids.get(toPickBottom)) : null;
        Card cTop2 = (toPickTop == 2) ? singleU.execute(match, player, (int) ids.get(toPickBottom + 1)) : null;

        // Apply the cards and remove them from the board.
        if (cBottom != null) {
            applyCard(cBottom, player, bottomRow);
        }
        if (cTop1 != null) {
            applyCard(cTop1, player, topRow);
        }
        if (cTop2 != null) {
            applyCard(cTop2, player, topRow);
        }
    }
}