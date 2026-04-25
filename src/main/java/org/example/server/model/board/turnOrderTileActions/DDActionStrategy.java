package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DDActionStrategy implements OfferActionStrategy {

    private final DPick singleD = new DPick();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // 1) Count how many pickable cards are in the bottom row
        int toPick = (int) Math.min(2, countPickable(match.getBoard().getBottomRow(), player));

        // 2) If there aren't any, throw exception to skip turn
        if (toPick == 0)
            throw new NoDrawableCardException("No drawable card in the bottom row, turn skipped.");

        // Try to pick the cards
        Card c1 = singleD.execute(match, player, ids.get(0));
        Card c2 = (toPick == 2) ? singleD.execute(match, player, ids.get(1)) : null;

        applyCard(c1, player, match.getBoard().getBottomRow());
        if (c2 != null)
            applyCard(c2, player, match.getBoard().getBottomRow());

    }
}
