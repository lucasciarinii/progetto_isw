package org.example.model.board;

import org.example.model.cards.eventCards.EventCard;
import org.example.model.match.Player;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    // ── Helper

    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new Player("Player" + i));
        }
        return players;
    }

}
