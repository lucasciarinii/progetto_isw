package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 2, Row 1
/**
 * Building that doubles shamanic ritual points for the top player.
 */
public class ShamanicDoublePointsBC extends BuildingCard {

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
    public ShamanicDoublePointsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.ShamanicDoublePointsBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: during shamanic ritual if you have the most stars get X2 of indicated points\n".formatted(super.toString());
    }

    /**
     * Effect is applied directly during the shamanic ritual event.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {
        //This building is checked directly during Shamanic Ritual event
    }
}
