package it.polimi.ingsw.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.client.view.TUI.ConsoleColors;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.interfaces.Visitor;

import java.util.Objects;

/**
 * Shaman character card.
 */
public class Shaman extends Character {

    /** Number of shaman stars this card provides. */
    private final int numStars;

    /**
     * Creates a shaman character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     * @param numStars number of stars
     */
    @SuppressWarnings("unused")
    public Shaman(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("numStars") int numStars) {
        super(id, era, CharacterType.SHAMAN);
        this.numStars = numStars;
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
     * @return number of shaman stars
     */
    public int getNumStars() {
        return numStars;
    }

    @Override
    public String toString() {
        return "%s%s\tstars: %s%s\n".formatted(ConsoleColors.PURPLE, super.toString(), this.numStars, ConsoleColors.RESET);

    }
}