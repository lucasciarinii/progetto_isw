package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

// Column 2, Row 8
/**
 * End-game building that grants a flat 25-point bonus.
 */
public class EndGameBonus25BC extends BuildingCard {
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
    public EndGameBonus25BC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.EndGameBonus25BC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get 25 points (end game)\n".formatted(super.toString());
    }

    /**
     * Adds the flat end-game bonus points to the owner.
     *
     * @param owner building owner
     * @param match current match
     */
    public void applyEffect(Player owner, Match match) { owner.addPoints(25);}
}
