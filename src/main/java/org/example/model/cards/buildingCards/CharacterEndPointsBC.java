package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;
import org.example.model.cards.characters.Character;


public class CharacterEndPointsBC extends BuildingCard {

    private final int pointsEffect;
    private final CharacterType characterEffect;

    public CharacterEndPointsBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("pointsEffect") int points, @JsonProperty("characterEffect") CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.pointsEffect = points;
        this.characterEffect = characterEffect;
	}

	@Override
	public void applyEffect(Player owner, Context context) {

        //Count the number of characters of the specified type owned by the player
        //and award points based on that count and the pointsEffect value.

        int matchingCharacters = 0;

        for (Character character : owner.getCharacters()) {
            if (character.getCharacterType() == characterEffect) {
                matchingCharacters++;
            }
        }

        owner.addPoints(matchingCharacters * pointsEffect);
    }
}