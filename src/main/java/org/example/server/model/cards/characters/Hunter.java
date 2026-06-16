package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;

/**
 * Hunter character card.
 */
public class Hunter extends Character {

    /** Whether this hunter can obtain food directly. */
    private final boolean obtainFood;

    /**
     * Creates a hunter character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     * @param obtainFood whether this hunter obtains food
     */
    @SuppressWarnings("unused")
    public Hunter(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("obtainFood") boolean obtainFood) {
        super(id, era, CharacterType.HUNTER);
        this.obtainFood = obtainFood;
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
     * @return true if the hunter can obtain food
     */
    public boolean isObtainFood() {
        return obtainFood;
    }

    @Override
    public String toString() {
        return "%s%s\tobtainFood: %s%s\n".formatted(ConsoleColors.RED, super.toString(), (obtainFood ? "YES" : "NO"), ConsoleColors.RESET);
    }
}