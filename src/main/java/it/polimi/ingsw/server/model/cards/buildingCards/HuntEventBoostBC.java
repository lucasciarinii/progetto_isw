package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

/**
 * Building that grants extra food and points during the Hunt event.
 */
public class HuntEventBoostBC extends BuildingCard {

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
    public HuntEventBoostBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.HuntEventBoostBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: during Hunt Event, for each  Hunter in your tribe get +1 points and +1 food\n".formatted(super.toString());
    }

    /**
     * Applies extra food and points based on the owner's hunters.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many hunters the player owns
        int hunters = owner.getHunters().size();

        //During the Hunt event, gain 1 additional food
        //and 1 additional prestige point for each hunter
        owner.addFood(hunters);
        owner.addPoints(hunters);
    }
}
