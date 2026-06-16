package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 2, Row 4
/**
 * Building that grants bonus food during the Cave Painting event.
 */
public class CavePaintingEventBoostBC extends BuildingCard {

    /**
     * Creates the building card from JSON data.
     *
     * @param id card id
     * @param era card era
     * @param foodCost food cost
     * @param endPoints end points
     * @param buildingCardType building type
     * @param isEndGame true if it scores at end game
     */
    @SuppressWarnings("unused")
    public CavePaintingEventBoostBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.CavePaintingEventBoostBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get 1 food for each artist in your tribe\n".formatted(super.toString());
    }

    /**
     * Applies the bonus food based on the owner's artists.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many artists the player owns
        int artists = owner.getArtists().size();

        //During the Cave Painting event, gain 1 food for each artist
        owner.addFood(artists);
    }
}
