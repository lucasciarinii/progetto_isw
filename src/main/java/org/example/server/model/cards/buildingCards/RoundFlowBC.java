package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 2, Row 7
/**
 * Building that allows drawing a top-row card after round resolution.
 */
public class RoundFlowBC extends BuildingCard {
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

    /**
     * Applies the building effect (resolved by the controller).
     *
     * @param owner building owner
     * @param match current match
     */
    public void applyEffect(Player owner, Match match) {
        // The controller will have to see which player owns this card and consequently invoke its card selection method from the top row
    }
}
