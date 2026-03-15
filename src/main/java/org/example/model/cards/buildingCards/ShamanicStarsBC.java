package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class ShamanicStarsBC extends BuildingCard {


    private static final int SHAMANIC_STARS_POINTS = 3;

    public ShamanicStarsBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame) {
        super(id, era, foodCost, endPoints, isEndGame);
    }

    //add Shamanic Stars to player
    @Override
    public void applyEffect(Player owner, Context context) {
        owner.addShamanStars(SHAMANIC_STARS_POINTS);
    }

}