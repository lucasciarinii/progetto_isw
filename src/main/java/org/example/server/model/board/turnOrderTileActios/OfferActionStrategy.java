package org.example.server.model.board.turnOrderTileActios;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public interface OfferActionStrategy {
    void execute(Match match, Player player, List<Integer> ids);
}
