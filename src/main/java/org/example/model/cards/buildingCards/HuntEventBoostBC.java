package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class HuntEventBoostBC extends BuildingCard {

    public HuntEventBoostBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, buildingCardType, isEndGame);
    }

    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many hunters the player owns
        int hunters = owner.getHunters().size();

        //During the Hunt event, gain 1 additional food
        //and 1 additional prestige point for each hunter
        owner.addFood(hunters);
        owner.addPoints(hunters);
    }
}
