package org.example.server.model.decks;

import org.example.server.model.cards.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for card decks split by era.
 *
 * @param <T> card type handled by the deck
 */
public abstract class Deck<T extends Card> {

    /** Cards for era I. */
    protected List<T> era_I_cards;
    /** Cards for era II. */
    protected List<T> era_II_cards;
    /** Cards for era III. */
    protected List<T> era_III_cards;

    /**
     * Initializes empty lists for each era.
     */
    public Deck() {
        this.era_I_cards = new ArrayList<>();
        this.era_II_cards = new ArrayList<>();
        this.era_III_cards = new ArrayList<>();
    }

}