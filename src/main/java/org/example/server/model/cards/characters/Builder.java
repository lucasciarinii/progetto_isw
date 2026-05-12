package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.util.Objects;

public class Builder extends Character {

    private final int discountBuilding;
    private final int endPoints;

    public Builder(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("discountBuilding") int discountBuilding, @JsonProperty("endPoints") int endPoints) {
        super(id, era, CharacterType.BUILDER);
        this.discountBuilding = discountBuilding;
        this.endPoints = endPoints;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {

        Objects.requireNonNull(visitor, "Visitor cannot be null");

        visitor.visit(this);
    }


    public int getDiscountBuilding() {
        return discountBuilding;
    }

    public int getEndPoints() {
        return endPoints;
    }

    @Override
    public String toString() {
        return "%s%s\tdiscount: %d\n\tendPoints: %d%s\n".formatted(ConsoleColors.GREY, super.toString(), this.discountBuilding, this.endPoints, ConsoleColors.RESET);
    }
}