package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Character;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.EnumMap;
import java.util.Map;

public class SetCollectionEndPointsBC extends BuildingCard {

    public SetCollectionEndPointsBC(
            @JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("class_type") BuildingCardType buildingCardType)
    {
        super(id, era, foodCost, endPoints, buildingCardType);
    }
    @Override
    public void applyEffect(Player owner, Match match) {

        //Count how many complete sets of 6 different character types
        //the player owns at the end of the game
        int completedSets = countCompletedSets(owner);

        //Award prestige points for each complete set
        owner.addPoints(completedSets * getEndPoints());
    }

    private int countCompletedSets(Player owner) {

        //Count how many characters the player owns for each type
        Map<CharacterType, Integer> counts = new EnumMap<>(CharacterType.class);

        for (CharacterType type : CharacterType.values()) {
            counts.put(type, 0);
        }

        for (Character character : owner.getOwnedCharacters()) {
            CharacterType type = character.getCharacterType();
            counts.put(type, counts.get(type) + 1);
        }

        //The number of complete sets is the minimum count
        //among all character types
        int completedSets = Integer.MAX_VALUE;

        for (CharacterType type : CharacterType.values()) {
            completedSets = Math.min(completedSets, counts.get(type));
        }

        return completedSets;
    }
}
