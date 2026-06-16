package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 1, Row 6
/**
 * Building that grants extra shamanic stars.
 */
public class ShamanicStarsBC extends BuildingCard {


    /** Bonus stars granted by this building. */
    private static final int SHAMANIC_STARS_POINTS = 3;

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

    /**
     * Applies the bonus stars to the owner.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {
        // It will be called just first time
        owner.addShamanStars(SHAMANIC_STARS_POINTS);
    }

}