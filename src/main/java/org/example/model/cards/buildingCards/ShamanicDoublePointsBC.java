package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col2 Row1
public class ShamanicDoublePointsBC extends BuildingCard {

    public ShamanicDoublePointsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType
    ) {
        super(id, era, foodCost, endPoints, buildingCardType);
    }

    @Override
    public void applyEffect(Player owner, Match match) {
        //This building is checked directly during Shamanic Ritual event
    }
}
