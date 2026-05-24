package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.cards.characters.Inventor;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.InventionType;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Building that rewards new pairs of inventors with the same invention.
 */
public class InventorPairsBC extends BuildingCard {

    /** Food granted for each new inventor pair. */
    private static final int FOOD_PER_NEW_PAIR = 3;
    /** True once the building has initialized its baseline state. */
    private boolean initialized = false;
    /** Number of pairs already rewarded. */
    private int rewardedPairs = 0;

    // Column 1, Row 5
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
    public InventorPairsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.InventorComboBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get +3 food each time you get an inventors pair with same invention\n".formatted(super.toString());
    }

    /**
     * Rewards food for new inventor pairs since the last check.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null");
        }


        if (!initialized) {

            initialized = true;
            rewardedPairs = getCurrentPairsOfInventors(owner);

            return;
        }


        int currentPairs = getCurrentPairsOfInventors(owner);
        int newPairs = currentPairs - rewardedPairs;

        if (newPairs > 0) {
            owner.addFood(newPairs * FOOD_PER_NEW_PAIR);
            rewardedPairs = currentPairs;
        }


    }

    /**
     * Counts current pairs of inventors by invention type.
     *
     * @param owner building owner
     * @return number of inventor pairs
     */
    private int getCurrentPairsOfInventors(Player owner) {

        // Maps the inventors by invention type and counts how many inventors
        // exists for each type.
        Map<InventionType, Long> inventorsByType = owner.getInventors().stream()
                .collect(Collectors.groupingBy(Inventor::getInvention, Collectors.counting()));


        // Count how many pairs exists in the map
        return inventorsByType.values().stream()
                .mapToInt(count -> (int) (count / 2))
                .sum();
    }

}