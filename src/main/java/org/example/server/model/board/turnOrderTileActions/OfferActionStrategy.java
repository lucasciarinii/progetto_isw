package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public interface OfferActionStrategy {
    void execute(Match match, Player player, int id);
}
