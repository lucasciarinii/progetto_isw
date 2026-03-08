package org.example.model.cards.characters;

import org.example.model.cards.Card;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;


public abstract class Character extends Card {


    public final CharacterType characterType;
    public boolean newCardInSet;


    public Character(Era era, CharacterType characterType, boolean newCardInSet) {
        super(era);
        this.characterType = characterType;
        this.newCardInSet = false;
    }


    public CharacterType getCharacterType() {
        return characterType;
    }


    public boolean getNewCardInSet() {
        return newCardInSet;
    }


    public void setNewCardInSet(boolean newCardInSet) {
        this.newCardInSet = newCardInSet;
    }


}
