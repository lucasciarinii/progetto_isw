package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;


public class ShamanicPointsBC extends BuildingCard {

    private final boolean shouldDoublePrestigePoints;

    public ShamanicPointsBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("shouldDoublePrestigePoints") boolean shouldDoublePrestigePoints) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.shouldDoublePrestigePoints = shouldDoublePrestigePoints;
    }

    public boolean shouldDoublePrestigePoints() {
        return shouldDoublePrestigePoints;
    }


    @Override
    public void applyEffect(Player owner, Match match) {

    }

}