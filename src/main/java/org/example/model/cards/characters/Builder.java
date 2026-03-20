package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;

public class Builder extends Character {

    private final int discountBuilding;
    private final int endPoints;

    public Builder(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("discountBuilding") int discountBuilding, @JsonProperty("endPoints") int endPoints) {
        super(id, era, characterType, newCardInSet);
        this.discountBuilding = discountBuilding;
        this.endPoints = endPoints;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


    public int getDiscountBuilding() {
        return discountBuilding;
    }

    public int getEndPoints() {
        return endPoints;
    }
}