package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;

import java.util.Objects;

public class Hunter extends Character {

    private final boolean obtainFood;

    public Hunter(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("obtainFood") boolean obtainFood) {
        super(id, era, characterType);
        this.obtainFood = obtainFood;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public boolean isObtainFood() {
        return obtainFood;
    }
}