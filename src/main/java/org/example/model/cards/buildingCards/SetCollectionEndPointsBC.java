package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

// Col2 Row5
public class SetCollectionEndPointsBC extends BuildingCard {

    private final static int END_POINTS = 6;

    public SetCollectionEndPointsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, buildingCardType, isEndGame);
    }

    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many complete sets of 6 different character types
        //the player owns at the end of the game
        int completedSets = countCompletedSets(owner);

        //Award prestige points for each complete set
        owner.addPoints(completedSets * END_POINTS);
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
