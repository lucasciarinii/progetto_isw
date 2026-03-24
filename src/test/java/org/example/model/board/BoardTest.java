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

    // ── Input Validation

    @Test
    @DisplayName("Constructor throws if players list is null")
    void testNullPlayersThrows() {
        assertThrows(NullPointerException.class, () -> new Board(null));
    }

    @Test
    @DisplayName("Constructor throws if player count is invalid (1 player)")
    void testInvalidPlayerCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Board(createPlayers(1)));
    }

    @Test
    @DisplayName("Constructor throws if player count is invalid (6 players)")
    void testTooManyPlayersThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Board(createPlayers(6)));
    }

    // ── OfferTrack ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OfferTrack has 4 tiles for 2 players")
    void testOfferTrackSize2Players() {
        Board board = new Board(createPlayers(2));
        assertEquals(4, board.getOfferTrack().size());
    }

    @Test
    @DisplayName("OfferTrack has 5 tiles for 3 players")
    void testOfferTrackSize3Players() {
        Board board = new Board(createPlayers(3));
        assertEquals(5, board.getOfferTrack().size());
    }

    @Test
    @DisplayName("OfferTrack has 6 tiles for 4 players")
    void testOfferTrackSize4Players() {
        Board board = new Board(createPlayers(4));
        assertEquals(6, board.getOfferTrack().size());
    }

    @Test
    @DisplayName("OfferTrack has 7 tiles for 5 players")
    void testOfferTrackSize5Players() {
        Board board = new Board(createPlayers(5));
        assertEquals(7, board.getOfferTrack().size());
    }

    // ── BottomRow ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("BottomRow has players.size() + 1 cards for 2 players")
    void testBottomRowSize2Players() {
        Board board = new Board(createPlayers(2));
        assertEquals(3, board.getBottomRow().size());
    }

    @Test
    @DisplayName("BottomRow has players.size() + 1 cards for 4 players")
    void testBottomRowSize4Players() {
        Board board = new Board(createPlayers(4));
        assertEquals(5, board.getBottomRow().size());
    }

    @Test
    @DisplayName("BottomRow contains no EventCards")
    void testBottomRowHasNoEventCards() {
        Board board = new Board(createPlayers(2));
        board.getBottomRow().forEach(card ->
                assertFalse(card instanceof EventCard,
                        "BottomRow non deve contenere EventCard, trovata: " + card.getClass().getSimpleName())
        );
    }

    // ── TopRow ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TopRow contains only EventCards after initialization")
    void testTopRowHasOnlyEventCards() {
        Board board = new Board(createPlayers(2));
        board.getTopRow().forEach(card ->
                assertTrue(card instanceof EventCard,
                        "TopRow deve contenere solo EventCard, trovata: " + card.getClass().getSimpleName())
        );
    }

    @Test
    @DisplayName("TopRow has players.size() + 4 cards for 2 players")
    void testTopRowSize2Players() {
        Board board = new Board(createPlayers(2));
        assertEquals(6, board.getTopRow().size());
    }

    // ── Decks not null ────────────────────────────────────────────────────────

    @Test
    @DisplayName("MainDeck is initialized and not null")
    void testMainDeckNotNull() {
        Board board = new Board(createPlayers(2));
        assertNotNull(board.getMainDeck());
    }

    @Test
    @DisplayName("BuildingDeck is initialized and not null")
    void testBuildingDeckNotNull() {
        Board board = new Board(createPlayers(2));
        assertNotNull(board.getBuildingDeck());
    }
}
