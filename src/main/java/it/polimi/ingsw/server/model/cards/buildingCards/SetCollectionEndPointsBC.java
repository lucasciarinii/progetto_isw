package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

// Column 2, Row 5
/**
 * End-game building that scores completed character sets.
 */
public class SetCollectionEndPointsBC extends BuildingCard {

    /** Points awarded for each completed set. */
    private static final int END_POINTS = 6;

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
    public SetCollectionEndPointsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.SetCollectionEndPointsBC, isEndGame);
    }

    @Override
    public String toString() {
        return "%s\tEffect: get +6 points for each completed set of characters (end game)\n".formatted(super.toString());

    }

    /**
     * Awards points for each completed character set owned by the player.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many complete sets of 6 different character types
        //the player owns at the end of the game
        int completedSets = countCompletedSets(owner);

        //Award prestige points for each complete set
        owner.addPoints(completedSets * END_POINTS);
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
        int gatherers = owner.getGatherers().size();
        int inventors = owner.getInventors().size();
        int builders = owner.getBuilders().size();
        int shamans = owner.getShamans().size();
        int artists = owner.getArtists().size();
        int hunters = owner.getHunters().size();

        return Math.min(
                Math.min(Math.min(inventors, gatherers), Math.min(shamans, builders)),
                Math.min(artists, hunters)
        );
    }
}
