package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;
import org.example.model.interfaces.Visitor;


public class Inventor extends Character {

    private final InventionType invention;
    private boolean wasPresentLastTurn;


    public Inventor(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet, @JsonProperty("invention") InventionType invention) {
        super(id, era, characterType, newCardInSet);
        this.invention = invention;
        this.wasPresentLastTurn = false;
    }


    // Double dispatch: delegates Character specific logic to the visitor.
    @Override
    public void accept(Visitor visitor) {
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







}
