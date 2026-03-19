package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Builder;
import org.example.model.enums.CharacterType;
import org.example.model.cards.characters.Character;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class EndGameBonusBC extends BuildingCard {
    public EndGameBonusBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
    }

    public void applyEffect(Player owner, Match match) {
        int punti_costruttore=0;
        for (Character card : owner.getOwnedCharacters())
        {
            if (card.getCharacterType()==CharacterType.BUILDER)
            {
                punti_costruttore += ((Builder) card).getEndPoints();
            }
        }
        owner.addPoints(punti_costruttore);
    }
}
