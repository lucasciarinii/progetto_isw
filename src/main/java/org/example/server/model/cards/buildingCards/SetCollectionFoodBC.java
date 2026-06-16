package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Column 1, Row 1
/**
 * Building that grants food when completing new character sets.
 */
public class SetCollectionFoodBC extends BuildingCard {

    /** Amount of food gained for each new complete set. */
    private static final int FOOD_PER_SET = 5;

    /** Number of complete sets already registered. */
    private int registeredSets;

    /** True once the building has initialized its baseline state. */
    private boolean initialized;

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
    public SetCollectionFoodBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.SetCollectionFoodBC, isEndGame);
        this.registeredSets = 0;
        this.initialized = false;
    }

    @Override
    public String toString() {
        return "%s\tEffect: get +5 food each time you complete a new character set\n".formatted(super.toString());

    }

    /**
     * Grants food for newly completed character sets.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many complete sets of 6 different character types
        //the player currently owns
        int currentCompletedSets = countCompletedSets(owner);

        //First activation after obtaining the building:
        //store already completed sets, but do not reward them
        if (!initialized) {
            registeredSets = currentCompletedSets;
            initialized = true;
            return;
        }

        //Compute how many new complete sets were formed
        //after the player acquired this building
        int newCompletedSets = currentCompletedSets - registeredSets;

        //Give food only for newly completed sets
        if (newCompletedSets > 0) {
            owner.addFood(newCompletedSets * FOOD_PER_SET);
            registeredSets = currentCompletedSets;
        }
    }

    /**
     * Counts the number of complete character sets owned by the player.
     *
     * @param owner building owner
     * @return number of complete sets
     */
    private int countCompletedSets(Player owner) {

        //A complete set contains one character of each type,
        //so the total number of complete sets is the minimum count
        //among all character type lists
        int inventors = owner.getInventors().size();
        int gatherers = owner.getGatherers().size();
        int shamans = owner.getShamans().size();
        int builders = owner.getBuilders().size();
        int artists = owner.getArtists().size();
        int hunters = owner.getHunters().size();

        return Math.min(
                Math.min(Math.min(inventors, gatherers), Math.min(shamans, builders)),
                Math.min(artists, hunters)
        );
    }
}
