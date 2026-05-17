package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Col1 Row4
public class RoundFlowTotemBC extends BuildingCard {

    private final static int FOOD_BONUS = 1;

    public RoundFlowTotemBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.RoundFlowTotemBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get +1 food if you return on a bonus tile\n".formatted(super.toString());

    }

    public void applyEffect(Player owner, Match match) {
        owner.addFood(FOOD_BONUS);
    }

}
