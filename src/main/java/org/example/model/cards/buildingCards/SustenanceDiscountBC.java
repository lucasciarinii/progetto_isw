package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Player;

public class SustenanceDiscountBC extends BuildingCard {

	public SustenanceDiscountBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame) {
		super(id, era, foodCost, endPoints, isEndGame);
	}

	@Override
	public void applyEffect(Player p) {

	}
}

