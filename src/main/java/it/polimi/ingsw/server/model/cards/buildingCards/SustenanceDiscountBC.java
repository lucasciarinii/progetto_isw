package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.Map;
import java.util.function.Function;

// Column 1, Row 2
/**
 * Building that grants a sustenance discount based on a character type.
 */
public class SustenanceDiscountBC extends BuildingCard {

    /** Maps supported character types to their sustenance discount logic. */
    private static final Map<CharacterType, Function<Player, Integer>> DISCOUNT_LOGIC = Map.of(
            CharacterType.INVENTOR, p -> p.getInventors().size(),
            CharacterType.GATHERER, p -> p.getGatherers().size(),
            CharacterType.ARTIST, p -> p.getArtists().size()
    );
    /** Character type that determines the discount. */
    private final CharacterType characterEffect;

    /**
     * Creates the building card from JSON data.
     *
     * @param id card id
     * @param era card era
     * @param foodCost food cost
     * @param endPoints end points
     * @param buildingCardType building type
     * @param characterEffect character type for the discount
     * @param isEndGame true if it scores at end game
     */
    @SuppressWarnings("unused")
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

    /**
     * Adds the sustenance discount to the owner.
     *
     * @param owner building owner
     * @param match current match
     */
    public void applyEffect(Player owner, Match match) {
        // Lookup the discount by character type, defaulting to 0 if unsupported.
        int discount = DISCOUNT_LOGIC.getOrDefault(characterEffect, _ -> 0).apply(owner);
        owner.addDiscountOnSustenance(discount);
    }
}
