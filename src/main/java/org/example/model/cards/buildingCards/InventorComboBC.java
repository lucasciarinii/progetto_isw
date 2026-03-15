package org.example.model.cards.buildingCards;

import org.example.model.board.Board;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class InventorComboBC extends BuildingCard{


    public InventorComboBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame) {
        super(id, era, foodCost, endPoints, isEndGame);
    }

    
    @Override
    public void applyEffect(Player owner, Context context) {

        int numOwnerCharacters = owner.getOwnedCharacters().size();


    }
    
}