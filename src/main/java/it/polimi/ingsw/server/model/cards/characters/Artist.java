package it.polimi.ingsw.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.client.view.TUI.ConsoleColors;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.interfaces.Visitor;

import java.util.Objects;

/**
 * Artist character card.
 */
public class Artist extends Character{


    /**
     * Creates an artist character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type from JSON
     */
    @SuppressWarnings("unused")
    public Artist(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType) {
        super(id, era, CharacterType.ARTIST);
    }

    /**
     * Accepts a visitor for double dispatch.
     *
     * @param visitor visitor instance
     */
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        // Double dispatch: delegates Character specific logic to the visitor.
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "%s%s%s".formatted(ConsoleColors.YELLOW, super.toString(), ConsoleColors.RESET);
    }
}
