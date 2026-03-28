package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col1 Row3
public class ShamanicNoMalusBC extends BuildingCard {

    public ShamanicNoMalusBC(
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
        //This building is checked directly during Shamanic Ritual event
    }
}
