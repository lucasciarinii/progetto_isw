package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;

public class Hunter extends Character {

    private final boolean obtainFood;

    public Hunter(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("obtainFood") boolean obtainFood) {
        super(id, era, characterType, newCardInSet);
        this.obtainFood = obtainFood;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(CardVisitor visitor) {
        visitor.visit(this);
    }


    public boolean isObtainFood() {
        return obtainFood;
    }
}