package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class EventBoostBC extends BuildingCard {
    private final CharacterType characterEffect;

    public EventBoostBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("characterEffect") CharacterType characterEffect) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.characterEffect = characterEffect;
    }

    @Override
    public void accept(CardVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void applyEffect(Player owner, Context context) {
    if (characterEffect==CharacterType.HUNTER)
    {
        int numero_cacciatori = (int) owner.getCharacters().stream()
                .filter(c -> c.getCharacterType()==CharacterType.HUNTER)
                .count();
        owner.addFood(numero_cacciatori);
        owner.addPoints(numero_cacciatori);
    }
    else
    {
        int numero_cacciatori = (int) owner.getCharacters().stream()
                .filter(c -> c.getCharacterType()==CharacterType.ARTIST)
                .count();
        owner.addFood(numero_cacciatori);
    }
    }


}
