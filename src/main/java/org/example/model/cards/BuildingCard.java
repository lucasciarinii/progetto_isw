package org.example.model.cards;

import org.example.model.enums.BuildingEffect;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;

public class BuildingCard extends Card {

    private final int foodCost;
    private final int endPoints;
    private final BuildingEffect effect;
    private final CharacterType effectCharacter;
    private final int effectPoints;

    public BuildingCard(Era era, int foodCost, int endPoints, BuildingEffect effect, CharacterType effectCharacter, int effectPoints) {
        super(era);
        this.foodCost = foodCost;
        this.endPoints = endPoints;
        this.effect = effect;
        this.effectCharacter = effectCharacter;
        this.effectPoints = effectPoints;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getEndPoints() {
        return endPoints;
    }

    public BuildingEffect getEffect() {
        return effect;
    }

    public CharacterType getEffectCharacter() {
        return effectCharacter;
    }

    public int getEffectPoints() {
        return effectPoints;
    }
}
