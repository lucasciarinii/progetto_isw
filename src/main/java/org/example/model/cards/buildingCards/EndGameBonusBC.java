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
    private final boolean shouldDoubleOnBuilders;
    public EndGameBonusBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("shouldDoubleOnBuilders") boolean s) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.shouldDoubleOnBuilders = s;
    }

    public void applyEffect(Player owner, Match match) {
    if (shouldDoubleOnBuilders)
    {
        int punti_costruttore=0;
        for (Character card : owner.getOwnedCharacters())
        {
            if (card.getCharacterType()==CharacterType.BUILDER)
            {
                punti_costruttore += ((Builder) card).getEndPoints();
            }
        }
        owner.addPoints(punti_costruttore); //da verificare bene come implementare questa funzione
                                            //se aggiungere il doppio dei punti e passare avanti o
                                            //calcolare i punti normalmente e poi sommarci questi
    }
    else
    {
        owner.addPoints(25);
    }
    }
}
