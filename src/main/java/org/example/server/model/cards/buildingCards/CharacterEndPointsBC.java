package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;
import java.util.stream.Stream;

// Col2 Row6
public class CharacterEndPointsBC extends BuildingCard {

    private final int pointsEffect;
    private final CharacterType characterEffect;

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