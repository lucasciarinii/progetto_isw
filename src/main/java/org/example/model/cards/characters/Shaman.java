package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class Shaman extends Character {

    private final int numStars;

    public Shaman(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("numStars") int numStars) {
        super(id, era, characterType, newCardInSet);
        this.numStars = numStars;
    }

    public int getNumStars() {
        return numStars;
    }


}