package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col1 Row1
public class SetCollectionFoodBC extends BuildingCard {

    //Amount of food gained for each new complete set
    private static final int FOOD_PER_SET = 5;

    //Number of complete sets already registered after obtaining this building
    private int registeredSets;

    //Used to understand if the building effect has already been initialized
    private boolean initialized;

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
