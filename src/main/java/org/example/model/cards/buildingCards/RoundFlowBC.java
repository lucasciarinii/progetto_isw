package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col2 Row7
public class RoundFlowBC extends BuildingCard {
    public RoundFlowBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.RoundFlowBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: in this round and in the next ones (after action resolving) get a top card\n".formatted(super.toString());
    }

    public void applyEffect(Player owner, Match match) {
        // The controller will have to see which player owns this card and consequently invoke its card selection method from the top row
    }
}
