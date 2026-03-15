package org.example.model.cards.buildingCards;

import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;
import org.example.model.board.Board;

public class CharacterEndPointsBC extends BuildingCard {
    private final int pointsEffect;
    private final CharacterType characterEffect;

    public CharacterEndPointsBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, int points, CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, isEndGame);
        this.pointsEffect = points;
        this.characterEffect = characterEffect;
	}

	@Override
	public void applyEffect(Player owner, Context context) {

	}
}

