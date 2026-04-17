package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.Map;
import java.util.function.Function;

// Col1 Row2
public class SustenanceDiscountBC extends BuildingCard {

    // Maps each supported character type to the function that computes the corresponding sustenance discount for the player.
    private static final Map<CharacterType, Function<Player, Integer>> DISCOUNT_LOGIC = Map.of(
            CharacterType.INVENTOR, p -> p.getInventors().size(),
            CharacterType.GATHERER, p -> p.getGatherers().size(),
            CharacterType.ARTIST, p -> p.getArtists().size()
    );
    private final CharacterType characterEffect;

    public SustenanceDiscountBC(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("foodCost") int foodCost,
            @JsonProperty("endPoints") int endPoints,
            @JsonProperty("class_type") BuildingCardType buildingCardType,
            @JsonProperty("characterEffect") CharacterType characterEffect,
            @JsonProperty("isEndGame") boolean isEndGame
    ) {
        super(id, era, foodCost, endPoints, BuildingCardType.SustenanceDiscountBC, isEndGame);
        this.characterEffect = characterEffect;
    }

    @Override
    public String toString() {
        return "%s\tEffect: during sustenance get a -1 food discount for each %s in your tribe\n".formatted(super.toString(), characterEffect);
    }

    public void applyEffect(Player owner, Match match) {
        // goes in the DICTOUN_LOGIC map to get the appropriate discount based on the characterEffect, defaulting to 0 if the character type is not supported. in order to pass to the map the owner we do apply(owner)
        int discount = DISCOUNT_LOGIC.getOrDefault(characterEffect, p -> 0).apply(owner);
        owner.addDiscountOnSustenance(discount);
    }
}
