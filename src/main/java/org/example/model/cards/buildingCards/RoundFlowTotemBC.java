package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class RoundFlowTotemBC extends BuildingCard {
    public RoundFlowTotemBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, buildingCardType);
    }

    public void applyEffect(Player owner, Match match) { owner.addFood(1);}

}
