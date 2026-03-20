package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class ShamanicStarsBC extends BuildingCard {


    private static final int SHAMANIC_STARS_POINTS = 3;

    public ShamanicStarsBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
    }


    @Override
    public void accept(CardVisitor visitor) {
        visitor.visit(this);
    }

    //add Shamanic Stars to player
    @Override
    public void applyEffect(Player owner, Context context) {
        owner.addShamanStars(SHAMANIC_STARS_POINTS);
    }

}