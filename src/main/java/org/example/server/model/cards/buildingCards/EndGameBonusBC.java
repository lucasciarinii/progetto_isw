package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.cards.characters.Builder;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 2, Row 3
/**
 * End-game building that doubles points from Builder cards.
 */
public class EndGameBonusBC extends BuildingCard {
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
    public EndGameBonusBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.EndGameBonusBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get double the points indicated in your Builder cards (end game)\n".formatted(super.toString());
    }

    /**
     * Adds bonus points based on the owner's Builder cards.
     *
     * @param owner building owner
     * @param match current match
     */
    public void applyEffect(Player owner, Match match) {
        int puntiCostruttore = owner.getBuilders().stream()
                .mapToInt(Builder::getEndPoints)
                .sum();

        owner.addPoints(puntiCostruttore);
    }
}
