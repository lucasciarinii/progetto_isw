package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;

public class SustenanceDiscountBC extends BuildingCard {
	private final CharacterType characterEffect;

	public SustenanceDiscountBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("characterEffect") CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
		this.characterEffect = characterEffect;
	}


	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public CharacterType getCharacterEffect() {
		return characterEffect;
	}

	@Override
	public void applyEffect(Player owner, Match match) {

	}
}

