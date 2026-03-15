package org.example.model.decks;

import org.example.model.cards.Card;

import java.util.NoSuchElementException;

public class MainDeck extends Deck<Card>{

    public MainDeck(int numPlayers) {

    }

    @Override
    public Card draw(){
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