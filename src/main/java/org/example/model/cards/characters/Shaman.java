package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;

public class Shaman extends Character {

    private final int numStars;

    public Shaman(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("numStars") int numStars) {
        super(id, era, characterType, newCardInSet);
        this.numStars = numStars;
    }


    @Override
    public void accept(CardVisitor visitor) {
        visitor.visit(this);
    }


    public int getNumStars() {
        return numStars;
    }


}