package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col2, Row4
public class CavePaintingEventBoostBC extends BuildingCard {

    public CavePaintingEventBoostBC(
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

        //Count how many artists the player owns
        int artists = owner.getArtists().size();

        //During the Cave Painting event, gain 1 food for each artist
        owner.addFood(artists);
    }
}
