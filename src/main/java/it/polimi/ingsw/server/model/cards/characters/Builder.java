package it.polimi.ingsw.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.client.view.TUI.ConsoleColors;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.interfaces.Visitor;

import java.util.Objects;

/**
 * Builder character card.
 */
public class Builder extends Character {

    /** Discount applied when buying buildings. */
    private final int discountBuilding;
    /** End-game points associated with the card. */
    private final int endPoints;

    /**
     * Creates a builder character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     * @param discountBuilding building discount value
     * @param endPoints end points
     */
    @SuppressWarnings("unused")
    public Builder(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("discountBuilding") int discountBuilding, @JsonProperty("endPoints") int endPoints) {
        super(id, era, CharacterType.BUILDER);
        this.discountBuilding = discountBuilding;
        this.endPoints = endPoints;
    }


    /**
     * Accepts a visitor for double dispatch.
     *
     * @param visitor visitor instance
     */
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    /**
     * @return building discount value
     */
    public int getDiscountBuilding() {
        return discountBuilding;
    }

    /**
     * @return end-game points from this card
     */
    public int getEndPoints() {
        return endPoints;
    }

    @Override
    public String toString() {
        return "%s%s\tdiscount: %d\n\tendPoints: %d%s\n".formatted(ConsoleColors.GREY, super.toString(), this.discountBuilding, this.endPoints, ConsoleColors.RESET);
    }
}