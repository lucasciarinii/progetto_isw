package org.example.model.cards.characters;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;


public class Gatherer extends Character {

    private static final int DISCOUNT_FACTOR = 3;


    public Gatherer(int id, Era era, CharacterType characterType, boolean newCardInSet) {
        super(id, era, characterType, newCardInSet);
    }


    public int getDiscountFactor() {
        return DISCOUNT_FACTOR;
    }


}
