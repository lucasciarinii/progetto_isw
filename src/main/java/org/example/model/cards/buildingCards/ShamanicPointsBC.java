package org.example.model.cards.buildingCards;

import org.example.model.board.Board;
import org.example.model.cards.eventCards.ShamanicRitual;
import org.example.model.enums.Era;
import org.example.model.match.Player;

import java.util.ArrayList;

public class ShamanicPointsBC extends BuildingCard {

    private final boolean type;

    public ShamanicPointsBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, boolean type) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.type = type;
    }

    @Override
    public void applyEffect(Player owner, Board board) {
        throw new UnsupportedOperationException();
    }


    private void applyShamanicRitualEffect(Player owner, Board board, ArrayList<Player> players, ShamanicRitual shamanicRitualEvent) {

    }

}