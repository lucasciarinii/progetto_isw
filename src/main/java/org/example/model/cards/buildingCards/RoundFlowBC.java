package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class RoundFlowBC extends BuildingCard {
    private final boolean type;
    public RoundFlowBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, boolean type) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.type = type;
    }

    public void applyEffect(Player owner, Context context) {

    }
}
