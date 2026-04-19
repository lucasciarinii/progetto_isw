package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

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
        super(id, era, foodCost, endPoints, BuildingCardType.ShamanicNoMalusBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: during shamanic ritual if you have least stars you don't lose points\n".formatted(super.toString());
    }

    @Override
    public void applyEffect(Player owner, Match match) {
        //This building is checked directly during Shamanic Ritual event
    }
}
