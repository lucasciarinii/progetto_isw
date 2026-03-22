package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Builder;
import org.example.model.enums.CharacterType;
import org.example.model.cards.characters.Character;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.Collection;

// Col2 Row3
public class EndGameBonusBC extends BuildingCard {
    public EndGameBonusBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, buildingCardType);
    }


    public void applyEffect(Player owner, Match match) {
        int puntiCostruttore = owner.getBuilders().stream()
                .mapToInt(Builder::getEndPoints)
                .sum();

        owner.addPoints(puntiCostruttore);
    }
}
