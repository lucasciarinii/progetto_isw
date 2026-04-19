package org.example.server.model.decks;

import org.example.server.model.board.Board;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildingDeckTest {

    // Test: verify that in case of null board, the constructor throws NullPointerException
    @Test
    @Tag("BuildingDeck")
    @DisplayName("verify that in case of null board, the constructor throws NullPointerException")
    void constructor_nullBoard_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BuildingDeck(2, null));
    }

    // Test (2 players): correct initialization of BuildingDeck, and verify that the correct number of cards are in the top row after initialization
	@Test
	@Tag("BuildingDeck")
	@DisplayName("correct initialization of BuildingDeck")
	void correctInitialization2Players() {
        //! 2 Players Case ========================================================================
        List<Player> players = List.of(new Player("Alice"), new Player("Bob"));
		Board b = new Board(players);

        // After initialization, total amount of BuildingDeck card should be 5 (6 total - 1 removed from era I)
        assertEquals(5, b.getBuildingDeck().era_I_cards.size() + b.getBuildingDeck().era_II_cards.size() + b.getBuildingDeck().era_III_cards.size());

        // In addition topRow cards should be number of players (2) + 4 + eraI buildings (1) = 7
        assertEquals(7, b.getTopRow().size());

        // In addition on the topRow there should be just Era I building card, and no Era II or Era III building cards
        assertTrue(b.getTopRow().stream().allMatch(card -> card.getEra() == Era.I));

        //! 3 Players Case ========================================================================
        players = List.of(new Player("Alice"), new Player("Bob"), new Player("Charlie"));
        b = new Board(players);

        // After initialization, total amount of BuildingDeck card should be 6 (8 total - 2 removed from era I)
        assertEquals(6, b.getBuildingDeck().era_I_cards.size() + b.getBuildingDeck().era_II_cards.size() + b.getBuildingDeck().era_III_cards.size());

        // In addition topRow cards should be number of players (3) + 4 + era I buildings (2) = 9
        assertEquals(9, b.getTopRow().size());

        // In addition on the topRow there should be just Era I building card, and no Era II or Era III building cards
        assertTrue(b.getTopRow().stream().allMatch(card -> card.getEra() == Era.I));

        //! 4 Players Case ========================================================================
        players = List.of(new Player("Alice"), new Player("Bob"), new Player("Charlie"), new Player("David"));
        b = new Board(players);

        // After initialization, total amount of BuildingDeck card should be 7 (9 total - 2 removed from era I)
        assertEquals(7, b.getBuildingDeck().era_I_cards.size() + b.getBuildingDeck().era_II_cards.size() + b.getBuildingDeck().era_III_cards.size());

        // In addition topRow cards should be number of players (4) + 4 + era I buildings (2) = 10
        assertEquals(10, b.getTopRow().size());

        // In addition on the topRow there should be just Era I building card, and no Era II or Era III building cards
        assertTrue(b.getTopRow().stream().allMatch(card -> card.getEra() == Era.I));

        //! 5 Players Case ========================================================================
        players = List.of(new Player("Alice"), new Player("Bob"), new Player("Charlie"), new Player("David"), new Player("Eve"));
        b = new Board(players);

        // After initialization, total amount of BuildingDeck card should be 8 (10 total - 2 removed from era I)
        assertEquals(8, b.getBuildingDeck().era_I_cards.size() + b.getBuildingDeck().era_II_cards.size() + b.getBuildingDeck().era_III_cards.size());

        // In addition topRow cards should be number of players (5) + 4 + era I buildings (2) = 11
        assertEquals(11, b.getTopRow().size());

        // In addition on the topRow there should be just Era I building card, and no Era II or Era III building cards
        assertTrue(b.getTopRow().stream().allMatch(card -> card.getEra() == Era.I));
    }

    // Test: addCardToTopRow(board, Era.II) moves all Era II cards to the top row
    @Test
    @Tag("BuildingDeck")
    @DisplayName("addCardToTopRow with Era II moves all Era II cards to the top row")
    void addCardToTopRow_eraII_movesAllEraIICards() {
        List<Player> players = List.of(new Player("Alice"), new Player("Bob"));
        Board b = new Board(players);
        BuildingDeck deck = b.getBuildingDeck();

        int initialTopRowSize = b.getTopRow().size();

        deck.addCardToTopRow(b, Era.II);

        assertEquals(initialTopRowSize + 2, b.getTopRow().size());
        assertTrue(b.getTopRow().subList(initialTopRowSize, b.getTopRow().size())
                .stream()
                .allMatch(card -> card.getEra() == Era.II));
        assertEquals(0, deck.era_II_cards.size());
    }

    // Test: addCardToTopRow(board, Era.III) moves all Era III cards to the top row
    @Test
    @Tag("BuildingDeck")
    @DisplayName("addCardToTopRow with Era III moves all Era III cards to the top row")
    void addCardToTopRow_eraIII_movesAllEraIIICards() {
        List<Player> players = List.of(new Player("Alice"), new Player("Bob"));
        Board b = new Board(players);
        BuildingDeck deck = b.getBuildingDeck();

        int initialTopRowSize = b.getTopRow().size();

        deck.addCardToTopRow(b, Era.III);

        assertEquals(initialTopRowSize + 3, b.getTopRow().size());
        assertTrue(b.getTopRow().subList(initialTopRowSize, b.getTopRow().size())
                .stream()
                .allMatch(card -> card.getEra() == Era.III));
        assertEquals(0, deck.era_III_cards.size());
    }

    // addCardToTopRow on same era twice: second call adds 0 cards
    @Test
    @DisplayName("addCardToTopRow on same era twice: second call adds 0 cards")
    void testAddCardToTopRowSameEraTwice() {
        List<Player> players = List.of(new Player("Alice"), new Player("Bob"));
        Board b = new Board(players);
        BuildingDeck deck = new BuildingDeck(2, b); // era II → 2 carte attese

        // First call: empty era II cards and add the, to the top row
        deck.addCardToTopRow(b, Era.II);
        int sizeAfterFirst = b.getTopRow().size();

        // Second call: era II card is already empty, so it should not add any card to the top row
        deck.addCardToTopRow(b, Era.II);
        int sizeAfterSecond = b.getTopRow().size();

        assertEquals(sizeAfterFirst, sizeAfterSecond,
                "La seconda chiamata sulla stessa era non deve aggiungere carte");
    }


}