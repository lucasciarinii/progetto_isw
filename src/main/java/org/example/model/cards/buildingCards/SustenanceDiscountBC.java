package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;

public class SustenanceDiscountBC extends BuildingCard {
	private final CharacterType characterEffect;

	public SustenanceDiscountBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, isEndGame);
		this.characterEffect = characterEffect;
	}

	@Override
	public void applyEffect(Player p) {

	}
}

