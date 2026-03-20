package org.example.model.cards;

public abstract class Card {

    private final int id;
    private final Era era;

    public Card(int id, Era era) {
        this.era = era;
        this.id = id;
    }



    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

}