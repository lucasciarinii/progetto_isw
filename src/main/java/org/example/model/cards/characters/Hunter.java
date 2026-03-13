package org.example.model.cards.characters;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class Hunter extends Character {

    private final boolean obtainFood;

    public Hunter(int id, Era era, CharacterType characterType, boolean newCardInSet, boolean obtainFood) {
        super(id, era, characterType, newCardInSet);
        this.obtainFood = obtainFood;
    }

    public boolean isObtainFood() {
        return obtainFood;
    }
}