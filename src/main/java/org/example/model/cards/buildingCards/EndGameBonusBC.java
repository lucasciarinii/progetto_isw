package org.example.model.cards.buildingCards;

import org.example.model.board.Board;
import org.example.model.enums.Era;
import org.example.model.match.Player;

public class EndGameBonusBC extends BuildingCard {
    private final boolean type;
    public EndGameBonusBC(Era era, int foodCost, int endPoints, boolean isEndGame, boolean type) {
        super(era, foodCost, endPoints, isEndGame);
        this.type = type;
    }

    public void applyEffect(Player player, Board board) {

    }
}
