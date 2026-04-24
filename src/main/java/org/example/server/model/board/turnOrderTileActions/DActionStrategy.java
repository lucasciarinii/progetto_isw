package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.board.Board;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

/* Commento Luca:
Secondo me la logica migliore è: usare due classi del tipo DPick e UPick che fanno quello che adesso attualmente fanno DActionStrategy e UActionStrategy
tranne per le ultime due azioni: ciò ritorneranno solo la carta da dover aggiungere (e non fanno l'azione effettiva)
e poi dentro a tutte e 6 le altre classi (Comprese DActionStrategy e UActionStrategy) se non sono state lanciate eccezioni (per errori vari),
aggiungere le carte al giocatore e rimuoverle dalla board. In questo modo si evita di aggiungere magari la prima carta,
senza sapere che la seconda vada effettivamente bene...
*/

public class DActionStrategy implements OfferActionStrategy {

    private final DPick singleD = new DPick();

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