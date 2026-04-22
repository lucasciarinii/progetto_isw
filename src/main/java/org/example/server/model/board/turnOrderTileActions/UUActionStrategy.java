package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;


public class UUActionStrategy implements OfferActionStrategy {

    private final OfferActionStrategy singleU = new UActionStrategy();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) {

        singleU.execute(match, player, ids.get(0));
        singleU.execute(match, player, ids.get(1));

    }

}