package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DUUActionStrategy implements OfferActionStrategy {

    private final UPick singleU = new UPick();
    private final DPick singleD = new DPick();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        List<Card> bottomRow = match.getBoard().getBottomRow();
        List<Card> topRow = match.getBoard().getTopRow();

        // 1) Count how many pickable cards are in top and bottom row
        int pickableBottom = (int) countPickable(bottomRow, player);
        int pickableTop = (int) countPickable(topRow, player);

        // 2) If there aren't any, throw exception to skip turn
        if (pickableBottom == 0 && pickableTop == 0)
            throw new NoDrawableCardException("No drawable cards (both in top and bottom row), turn skipped.");

        int toPickBottom = Math.min(1, pickableBottom);
        int toPickTop = Math.min(2, pickableTop);

        // 3) Try to pick the cards
        Card cBottom = (toPickBottom == 1) ? singleD.execute(match, player, ids.getFirst()) : null;
        Card cTop1 = (toPickTop >= 1) ? singleU.execute(match, player, ids.get(toPickBottom)) : null;
        Card cTop2 = (toPickTop == 2) ? singleU.execute(match, player, ids.get(toPickBottom + 1)) : null;

        if (cBottom != null)
            applyCard(cBottom, player, bottomRow);
        if (cTop1 != null)
            applyCard(cTop1, player, topRow);
        if (cTop2 != null)
            applyCard(cTop2, player, topRow);



    }
}
