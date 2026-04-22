package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class DDActionStrategy implements OfferActionStrategy {

    private final DActionStrategy singleD = new DActionStrategy();

    @Override
    public void execute(Match match, Player player, List<Integer> ids) {

        singleD.execute(match, player, ids.get(0));
        singleD.execute(match, player, ids.get(1));

    }


}
