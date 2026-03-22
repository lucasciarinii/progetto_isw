package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Inventor;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.Map;
import java.util.stream.Collectors;

public class InventorPairsBC extends BuildingCard {

    private static final int FOOD_PER_NEW_PAIR = 3;
    private boolean initialized = false;
    private int rewardedPairs = 0;

    // Col1 Row5
    public InventorPairsBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, buildingCardType);
    }

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