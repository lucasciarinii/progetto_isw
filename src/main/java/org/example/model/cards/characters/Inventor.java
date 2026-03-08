package org.example.model.cards.characters;

import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;


public class Inventor extends Character {

    private final InventionType invention;
    private boolean wasPresentLastTurn;


    public Inventor(Era era, CharacterType characterType, boolean newCardInSet, InventionType invention) {
        super(era, characterType, newCardInSet);
        this.invention = invention;
        this.wasPresentLastTurn = false;
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
