package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;


public class Gatherer extends Character {

    private static final int DISCOUNT_FACTOR = 3;


    public Gatherer(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType) {
        super(id, era, CharacterType.GATHERER);
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public int getDiscountFactor() {
        return DISCOUNT_FACTOR;
    }

    @Override
    public String toString() {
        return "%s%s%s".formatted(ConsoleColors.ORANGE, super.toString(), ConsoleColors.RESET);
    }
}
