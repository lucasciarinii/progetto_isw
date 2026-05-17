package org.example.server.model.decks;

import org.example.server.model.cards.Card;
import org.example.server.model.enums.Era;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MainDeckTest {

    //* TEST CASES

    // Test: Constructor with valid player count does not throw exceptions and allows successful draw
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: Constructor with valid player count does not throw and allows successful draw")
    void constructor_validNumPlayers_createsDeck() {
        // Arrange & Act
        MainDeck mainDeck = new MainDeck(2);
        Card drawnCard = mainDeck.draw();

        // Assert: constructor succeeds, JSON is reachable and deserialized
        assertNotNull(mainDeck);
        assertNotNull(drawnCard);
    }

    // Test: draw() progresses through era I, II, III and throws NoSuchElementException when empty
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: draw() progresses through era I, II, III and throws NoSuchElementException when empty")
    void draw_progressesThroughEras_throwsWhenEmpty() {
        // Arrange
        MainDeck mainDeck = new MainDeck(2);
        
        int era_I_count = mainDeck.era_I_cards.size();
        int era_II_count = mainDeck.era_II_cards.size();
        int era_III_count = mainDeck.era_III_cards.size();

        // Act & Assert: Draw all era I cards
        for (int i = 0; i < era_I_count; i++) {
            Card drawn = mainDeck.draw();
            assertNotNull(drawn, "Era I card should not be null");
            assertEquals(Era.I, drawn.getEra(), "Drawn card should be from Era I");
        }
        assertEquals(0, mainDeck.era_I_cards.size(), "Era I should be empty after all draws");

        // Act & Assert: Draw all era II cards
        for (int i = 0; i < era_II_count; i++) {
            Card drawn = mainDeck.draw();
            assertNotNull(drawn, "Era II card should not be null");
            assertEquals(Era.II, drawn.getEra(), "Drawn card should be from Era II");
        }
        assertEquals(0, mainDeck.era_II_cards.size(), "Era II should be empty after all draws");

        // Act & Assert: Draw all era III cards
        for (int i = 0; i < era_III_count; i++) {
            Card drawn = mainDeck.draw();
            assertNotNull(drawn, "Era III card should not be null");
            assertEquals(Era.III, drawn.getEra(), "Drawn card should be from Era III");
        }
        assertEquals(0, mainDeck.era_III_cards.size(), "Era III should be empty after all draws");

        // Act & Assert: Attempt to draw from empty deck
        assertThrows(NoSuchElementException.class, () -> mainDeck.draw(), 
                     "Should throw NoSuchElementException when deck is empty");
    }

    // Test: draw() reduces total remaining cards by exactly 1
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: draw() reduces total remaining cards by one")
    void draw_reducesRemainingCardsByOne() {
        MainDeck mainDeck = new MainDeck(2);

        int totalBeforeDraw = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        Card drawnCard = mainDeck.draw();

        assertNotNull(drawnCard);
        assertFalse(mainDeck.era_I_cards.contains(drawnCard));
        assertFalse(mainDeck.era_II_cards.contains(drawnCard));
        assertFalse(mainDeck.era_III_cards.contains(drawnCard));

        int totalAfterDraw = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        assertEquals(totalBeforeDraw - 1, totalAfterDraw);
    }

    // Test: MainDeck must contain the correct number of cards for different player counts
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: Correct number of cards in MainDeck for different player counts")
    void testCorrectNumberCards() {
        // MainDeck cards:
        // 2 players -> 63 total (51 characters, 12 events)
        // 3 players -> 74 total (62 characters, 12 events)
        // 4 players -> 85 total (73 characters, 12 events)
        // 5 players -> 96 total (84 characters, 12 events)

        // 2 Players case
        MainDeck mainDeck = new MainDeck(2);
        assertEquals(63, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());

        // 3 Players case
        mainDeck = new MainDeck(3);
        assertEquals(74, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());

        // 4 Players case
        mainDeck = new MainDeck(4);
        assertEquals(85, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());

        // 5 Players case
        mainDeck = new MainDeck(5);
        assertEquals(96, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());
    }

    // Test: MainDeck must contain (for each configuration) all card with different ids (no duplicates)
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: ALL ID's in MainDeck are unique (no duplicates)")
    void testNoDuplicateIds() {

        // 2 Players case
        MainDeck mainDeck = new MainDeck(2);
        int totalCards = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        Set<Integer> uniqueIds = new HashSet<>();
        for (Card card : mainDeck.era_I_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_II_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_III_cards) uniqueIds.add(card.getId());
        assertEquals(totalCards, uniqueIds.size());

        // 3 Players case
        mainDeck = new MainDeck(3);
        totalCards = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        uniqueIds = new HashSet<>();
        for (Card card : mainDeck.era_I_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_II_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_III_cards) uniqueIds.add(card.getId());
        assertEquals(totalCards, uniqueIds.size());

        // 4 Players case
        mainDeck = new MainDeck(4);
        totalCards = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        uniqueIds = new HashSet<>();
        for (Card card : mainDeck.era_I_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_II_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_III_cards) uniqueIds.add(card.getId());
        assertEquals(totalCards, uniqueIds.size());

        // 5 Players case
        mainDeck = new MainDeck(5);
        totalCards = mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size();
        uniqueIds = new HashSet<>();
        for (Card card : mainDeck.era_I_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_II_cards) uniqueIds.add(card.getId());
        for (Card card : mainDeck.era_III_cards) uniqueIds.add(card.getId());
        assertEquals(totalCards, uniqueIds.size());
    }


}