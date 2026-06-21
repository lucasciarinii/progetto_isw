package it.polimi.ingsw.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.client.view.TUI.ConsoleColors;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.InventionType;
import it.polimi.ingsw.server.model.interfaces.Visitor;

import java.util.Objects;


/**
 * Inventor character card.
 */
public class Inventor extends Character {

    /** Invention associated with the inventor. */
    private final InventionType invention;


    /**
     * Creates an inventor character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     * @param invention invention type
     */
    @SuppressWarnings("unused")
    public Inventor(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("invention") InventionType invention) {
        super(id, era, CharacterType.INVENTOR);
        this.invention = invention;
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
     * @return invention type
     */
    public InventionType getInvention() {
        return invention;
    }


    @Override
    public String toString() {
        return "%s%s\tinvention: %s%s\n".formatted(ConsoleColors.MINT, super.toString(), invention, ConsoleColors.RESET);

    }
}
