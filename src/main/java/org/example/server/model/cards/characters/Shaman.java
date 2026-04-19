package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;

public class Shaman extends Character {

    private final int numStars;

    public Shaman(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("numStars") int numStars) {
        super(id, era, CharacterType.SHAMAN);
        this.numStars = numStars;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public int getNumStars() {
        return numStars;
    }

    @Override
    public String toString() {
        return "%s%s\tstars: %s%s\n".formatted(ConsoleColors.PURPLE, super.toString(), this.numStars, ConsoleColors.RESET);

    }
}