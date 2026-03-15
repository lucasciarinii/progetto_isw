package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

import java.util.ArrayList;

public class ShamanicPointsBC extends BuildingCard {

    private final boolean shouldDoublePrestigePoints;

    public ShamanicPointsBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, boolean shouldDoublePrestigePoints) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.shouldDoublePrestigePoints = shouldDoublePrestigePoints;
    }

    @Override
    public void applyEffect(Player owner, Context context) {

    }

}