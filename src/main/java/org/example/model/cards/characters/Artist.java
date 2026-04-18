package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.ConsoleColors;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;

import java.util.Objects;

public class Artist extends Character{


    public Artist(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType) {
        super(id, era, CharacterType.ARTIST);
    }

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
