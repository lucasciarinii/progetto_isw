package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class ShamanicStarsBC extends BuildingCard {


    private static final int SHAMANIC_STARS_POINTS = 3;

    public ShamanicStarsBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, buildingCardType);
    }



    //add Shamanic Stars to player
    @Override
    public void applyEffect(Player owner, Match match) {
        owner.addShamanStars(SHAMANIC_STARS_POINTS);
    }

}