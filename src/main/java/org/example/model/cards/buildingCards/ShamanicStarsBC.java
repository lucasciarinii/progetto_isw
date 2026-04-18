package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col1 Row6
public class ShamanicStarsBC extends BuildingCard {


    private static final int SHAMANIC_STARS_POINTS = 3;

    public ShamanicStarsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.ShamanicStarsBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: during shamanic ritual get +3 stars\n".formatted(super.toString());
    }

    //add Shamanic Stars to player
    @Override
    public void applyEffect(Player owner, Match match) {
        // It will be called just first time
        owner.addShamanStars(SHAMANIC_STARS_POINTS);
    }

}