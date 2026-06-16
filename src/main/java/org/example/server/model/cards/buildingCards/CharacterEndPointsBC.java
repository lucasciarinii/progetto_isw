package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;
import java.util.stream.Stream;

// Column 2, Row 6
/**
 * End-game building that grants points per character of a given type.
 */
public class CharacterEndPointsBC extends BuildingCard {

    /** Points awarded for each matching character. */
    private final int pointsEffect;
    /** Character type that triggers the bonus. */
    private final CharacterType characterEffect;

    /**
     * Creates the building card from JSON data.
     *
     * @param id card id
     * @param era card era
     * @param foodCost food cost
     * @param endPoints end points
     * @param buildingCardType building type
     * @param points points per matching character
     * @param characterEffect character type that scores
     * @param isEndGame true if it scores at end game
     */
    @SuppressWarnings("unused")
    public CharacterEndPointsBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("pointsEffect") int points,
            @JsonProperty("characterEffect") CharacterType characterEffect,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.CharacterEndPointsBC, isEndGame);
        this.pointsEffect = points;
        this.characterEffect = characterEffect;
    }

    @Override
    public String toString() {
        return "%s\tEffect: get %d points for each %s in your tribe (end game)\n".formatted(super.toString(), pointsEffect, characterEffect);
    }

    /**
     * Adds points based on the owner's characters of the configured type.
     *
     * @param owner building owner
     * @param match current match
     */
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count the number of characters of the specified type owned by the player
        //and award points based on that count and the pointsEffect value.

        long matchingCharacters = Stream.of(owner.getInventors(), owner.getGatherers(), owner.getShamans(), owner.getBuilders(), owner.getArtists(), owner.getHunters())
                .flatMap(List::stream) // "Crush" the 6 lists into a single stream
                .filter(character -> character.getCharacterType() == characterEffect) // Keep only characters of the specified type
                .count();

        owner.addPoints(((int) matchingCharacters) * pointsEffect);
    }
}