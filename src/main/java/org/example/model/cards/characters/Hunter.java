package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class Hunter extends Character {

    private final boolean obtainFood;

    public Hunter(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("obtainFood") boolean obtainFood) {
        super(id, era, characterType, newCardInSet);
        this.obtainFood = obtainFood;
    }

    public boolean isObtainFood() {
        return obtainFood;
    }
}