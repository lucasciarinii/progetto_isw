package org.example.model.cards.buildingCards;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Player;

public class EventBoostBC extends BuildingCard {
    private final CharacterType characterType;

    public EventBoostBC(Era era, int foodCost, int endPoints, boolean isEndGame, CharacterType characterType) {
        super(era, foodCost, endPoints, isEndGame);
        this.characterType = characterType;
    }

    @Override
    public void applyEffect(Player p) {

    }


}
