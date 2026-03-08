package org.example.model.cards.characters;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class Shaman extends Character {

    private final int numStars;

    public Shaman(Era era, CharacterType characterType, boolean newCardInSet, int numStars) {
        super(era, characterType, newCardInSet);
        this.numStars = numStars;
    }

    public int getNumStars() {
        return numStars;
    }


}