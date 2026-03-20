package org.example.model.cards;

import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;
import org.example.model.interfaces.Visitable;

public abstract class Card implements Visitable {

    private final int id;
    private final Era era;

    public Card(int id, Era era) {
        this.era = era;
        this.id = id;
    }

    public abstract void accept(CardVisitor visitor);


    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

}