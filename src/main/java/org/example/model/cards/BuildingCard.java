package org.example.model.cards;


import org.example.model.enums.Era;
import org.example.model.match.Player;

public abstract class BuildingCard extends Card {

    private final int foodCost;
    private final int endPoints;
    private final boolean isEndGame;

    public BuildingCard(Era era, int foodCost, int endPoints, boolean isEndGame) {
        super(era);
        this.foodCost = foodCost;
        this.endPoints = endPoints;
        this.isEndGame = isEndGame;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getEndPoints() {
        return endPoints;
    }

    public boolean isEndGame() {
        return isEndGame;
    }

    public abstract void applyEffect(Player p);

}
