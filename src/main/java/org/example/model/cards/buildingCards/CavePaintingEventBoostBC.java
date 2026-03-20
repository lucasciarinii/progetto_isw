package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Character;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class CavePaintingEventBoostBC extends BuildingCard {

    public CavePaintingEventBoostBC(
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

        //Count how many artists the player owns
        int artists = 0;

        for (Character character : owner.getOwnedCharacters()) {
            if (character.getCharacterType() == CharacterType.ARTIST) {
                artists++;
            }
        }

        //During the Cave Painting event, gain 1 food for each artist
        owner.addFood(artists);
    }
}
