package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;


public class Gatherer extends Character {

    private static final int DISCOUNT_FACTOR = 3;


    public Gatherer(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet) {
        super(id, era, characterType, newCardInSet);
    }


    public int getDiscountFactor() {
        return DISCOUNT_FACTOR;
    }


}
