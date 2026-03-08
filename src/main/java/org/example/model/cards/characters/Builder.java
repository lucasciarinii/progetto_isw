package org.example.model.cards.characters;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class Builder extends Character {

    private final int discountBuilding;
    private final int endPoints;

    public Builder(Era era, CharacterType characterType, boolean newCardInSet, int discountBuilding, int endPoints) {
        super(era, characterType, newCardInSet);
        this.discountBuilding = discountBuilding;
        this.endPoints = endPoints;
    }

    public int getDiscountBuilding() {
        return discountBuilding;
    }

    public int getEndPoints() {
        return endPoints;
    }
}