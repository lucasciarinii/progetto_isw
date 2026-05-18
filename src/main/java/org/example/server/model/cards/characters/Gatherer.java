package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;


/**
 * Gatherer character card.
 */
public class Gatherer extends Character {

    /** Discount factor used by gatherers. */
    private static final int DISCOUNT_FACTOR = 3;


    /**
     * Creates a gatherer character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     */
    public Gatherer(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType) {
        super(id, era, CharacterType.GATHERER);
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
     * @return gatherer discount factor
     */
    public int getDiscountFactor() {
        return DISCOUNT_FACTOR;
    }

    @Override
    public String toString() {
        return "%s%s%s".formatted(ConsoleColors.ORANGE, super.toString(), ConsoleColors.RESET);
    }
}
