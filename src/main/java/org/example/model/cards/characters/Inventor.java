package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.ConsoleColors;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;
import org.example.model.interfaces.Visitor;

import java.util.Objects;


public class Inventor extends Character {

    private final InventionType invention;
    private boolean wasPresentLastTurn = false;


    public Inventor(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("invention") InventionType invention) {
        super(id, era, CharacterType.INVENTOR);
        this.invention = invention;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public InventionType getInvention() {
        return invention;
    }


    public boolean isWasPresentLastTurn() {
        return wasPresentLastTurn;
    }

    public void setWasPresentLastTurn(boolean wasPresentLastTurn) {
        this.wasPresentLastTurn = wasPresentLastTurn;
    }

    @Override
    public String toString() {
        return "%s%s\tinvention: %s%s\n".formatted(ConsoleColors.MINT, super.toString(), invention, ConsoleColors.RESET);

    }
}
