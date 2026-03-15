package org.example.model.cards.buildingCards;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class SustenanceDiscountBC extends BuildingCard {
	private final CharacterType characterEffect;

	public SustenanceDiscountBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, isEndGame);
		this.characterEffect = characterEffect;
	}

	@Override
	public void applyEffect(Player owner, Context context) {

	}
}

