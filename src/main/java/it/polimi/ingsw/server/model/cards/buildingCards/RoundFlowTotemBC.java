package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

// Column 1, Row 4
/**
 * Building that grants food when returning to a bonus tile.
 */
public class RoundFlowTotemBC extends BuildingCard {

    /** Food bonus when the effect triggers. */
    private static final int FOOD_BONUS = 1;

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

    /**
     * Grants the bonus food to the owner.
     *
     * @param owner building owner
     * @param match current match
     */
    public void applyEffect(Player owner, Match match) {
        owner.addFood(FOOD_BONUS);
    }

}
