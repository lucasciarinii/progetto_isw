package org.example.model.decks;

import org.example.model.cards.Card;

import java.util.ArrayList;
import java.util.List;

public abstract class Deck<T extends Card> {

    protected final List<T> era_I_cards;
    protected final List<T> era_II_cards;
    protected final List<T> era_III_cards;

    public Deck() {
        this.era_I_cards = new ArrayList<T>();
        this.era_II_cards = new ArrayList<T>();
        this.era_III_cards = new ArrayList<T>();
    }

    public abstract T draw();
}