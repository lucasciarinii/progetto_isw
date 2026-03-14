package org.example.model.cards.buildingCards;

import org.example.model.board.Board;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Player;

public class EventBoostBC extends BuildingCard {
    private final CharacterType characterType;

    public EventBoostBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, CharacterType characterType) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.characterType = characterType;
    }

    @Override
    public void applyEffect(Player owner, Board b) {

    }


}
