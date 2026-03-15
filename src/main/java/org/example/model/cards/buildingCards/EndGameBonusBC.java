package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class EndGameBonusBC extends BuildingCard {
    private final boolean shouldDoubleOnBuilders;
    public EndGameBonusBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, boolean s) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.shouldDoubleOnBuilders = s;
    }

    public void applyEffect(Player owner, Context context) {

    }
}
