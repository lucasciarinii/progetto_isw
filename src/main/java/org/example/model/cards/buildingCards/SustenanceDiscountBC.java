package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.model.enums.CharacterType;
import org.example.model.cards.characters.Character;

public class SustenanceDiscountBC extends BuildingCard {
	private final CharacterType characterEffect;

	public SustenanceDiscountBC(@JsonProperty("id") int id,
								@JsonProperty("era") Era era,
								@JsonProperty("foodCost") int foodCost,
								@JsonProperty("endPoints") int endPoints,
								@JsonProperty("class_type") BuildingCardType buildingCardType,
								@JsonProperty("characterEffect") CharacterType characterEffect) {
		super(id, era, foodCost, endPoints, buildingCardType);
		this.characterEffect = characterEffect;
	}



	public CharacterType getCharacterEffect() {
		return characterEffect;
	}

	@Override
	public void applyEffect(Player owner, Match match) {

		//Count how many owned characters match the type required by this building
		int discount = 0;

		for (Character character : owner.getOwnedCharacters()) {
			if (character.getCharacterType() == characterEffect) {
				discount++;
			}
		}

		//Add the computed discount to the player's sustenance discount
		if (discount > 0) {
			owner.addDiscountOnSustenance(discount);
		}
	}
}