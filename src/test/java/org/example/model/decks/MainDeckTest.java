package org.example.model.decks;

import org.example.model.cards.Card;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainDeckTest {
    
    @Test
    @Tag("MainDeck")
    @DisplayName("Test: Correct number of cards in MainDeck for different player counts")
    void testCorrectNumberCards() {
        // Carte del MainDeck sono:
        // 2 giocatori -> 63 carte totali (51 personaggi, 12 eventi)
        // 3 giocatori -> 74 carte totali (62 personaggi, 12 eventi)
        // 4 giocatori -> 85 carte totali (73 personaggi, 12 eventi)
        // 5 giocatori -> 96 carte totali (84 personaggi, 12 eventi)

        // Istanzia MainDeck con 2 giocatori
        MainDeck mainDeck = new MainDeck(2);
        assertEquals(63, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());

        // TODO: implements in MainDeck json for 3+ players and uncomment the following tests
//        // Istanzia MainDeck con 3 giocatori
//        mainDeck = new MainDeck(3);
//        assertEquals(74, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());
//
//        // Istanzia MainDeck con 4 giocatori
//        mainDeck = new MainDeck(4);
//        assertEquals(85, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());
//
//        // Istanzia MainDeck con 5 giocatori
//        mainDeck = new MainDeck(5);
//        assertEquals(96, mainDeck.era_I_cards.size() + mainDeck.era_II_cards.size() + mainDeck.era_III_cards.size());
    }

    @Test
    @Tag("MainDeck")
    @DisplayName("Test: draw removes the card drawn from all lists in MainDeck")
    void testDrawRemovesDrawnCardFromEveryList() {
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

    // Creato un deck, la prima carta deve essere di era I
    // poi continuo a pescare ed esaurite quelle di era I devono essere di II e poi stesso discorso per III
}