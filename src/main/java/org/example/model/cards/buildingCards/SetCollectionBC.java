package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Player;
import org.example.model.board.Board;

public class SetCollectionBC extends BuildingCard {

    public SetCollectionBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame) {
        super(id, era, foodCost, endPOints, isEndGame);
    }

    @Override
    public void applyEffect(Player p) {

    }
}
