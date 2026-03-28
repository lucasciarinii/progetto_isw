package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;

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
		super(id, era, foodCost, endPoints, buildingCardType, isEndGame);
        this.pointsEffect = points;
        this.characterEffect = characterEffect;
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