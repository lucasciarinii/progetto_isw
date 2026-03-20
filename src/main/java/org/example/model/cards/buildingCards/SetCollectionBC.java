package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Character;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.EnumMap;
import java.util.Map;

public class SetCollectionBC extends BuildingCard {

    //Amount of food gained for each new complete set
    private static final int FOOD_PER_SET = 5;

    //Number of complete sets already registered after obtaining this building
    private int registeredSets;

    //Used to understand if the building effect has already been initialized
    private boolean initialized;

    public SetCollectionBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.registeredSets = 0;
        this.initialized = false;
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many complete sets of 6 different character types
        //the player currently owns

        int currentCompletedSets = countCompletedSets(owner);

        //If this is the end-game version of the building,
        //award prestige points based on the total number of completed sets

        if (isEndGame()) {
            owner.addPoints(currentCompletedSets * getEndPoints());
            return;
        }

        //First activation after obtaining the building:
        //store already completed sets, but do not give food for them

        if (!initialized) {
            registeredSets = currentCompletedSets;
            initialized = true;
            return;
        }

        // Compute how many new sets were completed after acquiring the building

        int newCompletedSets = currentCompletedSets - registeredSets;

        //Give food only for newly completed sets

        if (newCompletedSets > 0) {
            owner.addFood(newCompletedSets * FOOD_PER_SET);
            registeredSets = currentCompletedSets;
        }
    }

    private int countCompletedSets(Player owner) {

        //Count how many characters the player has for each character type

        Map<CharacterType, Integer> counts = new EnumMap<>(CharacterType.class);

        for (CharacterType type : CharacterType.values()) {
            counts.put(type, 0);
        }

        for (Character character : owner.getCharacters()) {
            CharacterType type = character.getCharacterType();
            counts.put(type, counts.get(type) + 1);
        }

        //The number of complete sets is the minimum count
        //among all character types

        int completedSets = Integer.MAX_VALUE;

        for (CharacterType type : CharacterType.values()) {
            completedSets = Math.min(completedSets, counts.get(type));
        }

        //If for some reason no type was processed, return 0

        return completedSets == Integer.MAX_VALUE ? 0 : completedSets;
    }
}
