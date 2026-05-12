package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;

public class Hunter extends Character {

    private final boolean obtainFood;

    public Hunter(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("obtainFood") boolean obtainFood) {
        super(id, era, CharacterType.HUNTER);
        this.obtainFood = obtainFood;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public boolean isObtainFood() {
        return obtainFood;
    }

    @Override
    public String toString() {
        String RED = "\u001B[31m";
        String RESET = "\u001B[0m";
        return "%s%s\tobtainFood: %s%s\n".formatted(ConsoleColors.RED, super.toString(), (obtainFood ? "YES" : "NO"), ConsoleColors.RESET);
    }
}