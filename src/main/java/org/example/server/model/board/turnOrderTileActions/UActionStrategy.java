package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.cards.Card;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class UActionStrategy implements OfferActionStrategy {

    private final UPick singleU = new UPick();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) throws NoDrawableCardException, InvalidCardException {
        // 1) Check if row has drawable cards
        int pickable = (int) countPickable(match.getBoard().getTopRow(), player);

        // 2) If not, throw exception to skip turn
        if (pickable == 0)
            throw new NoDrawableCardException("No drawable card in the top row, turn skipped.");

        // 3) Try to pick the card - if ID is not valid, InvalidCardException -> client will have to choose again
        Card c1 = singleU.execute(match, player, ids.getFirst());

        // No possible exception from here on, so we can add the card to the player and remove it from the board
        applyCard(c1, player, match.getBoard().getTopRow());
    }
}