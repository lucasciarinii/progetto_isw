package org.example.model.decks;

import org.example.model.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class Deck<T extends Card> {

    private final List<T> era_I_cards;
    private final List<T> era_II_cards;
    private final List<T> era_III_cards;

    public Deck() {
        this.era_I_cards = new ArrayList<T>();
        this.era_II_cards = new ArrayList<T>();
        this.era_III_cards = new ArrayList<T>();

    }

    public T draw() {
        if (!era_I_cards.isEmpty()) {
            return era_I_cards.remove(0);
        }
        if (!era_II_cards.isEmpty()) {
            return era_II_cards.remove(0);
        }
        if (!era_III_cards.isEmpty()) {
            return era_III_cards.remove(0);
        }

        throw new NoSuchElementException("No cards left in deck");
    }
}