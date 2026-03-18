package org.example.model.cards;

import org.example.model.enums.Era;
import org.example.model.match.Player;

public abstract class Card {

    private final int id;
    private final Era era;

    public Card(int id, Era era) {
        this.era = era;
        this.id = id;
    }

    /**
     * Double Dispatch: ogni sottotipo conosce la lista giusta in cui inserirsi.
     * Il chiamante usa semplicemente player.addCard(card) senza mai fare
     * instanceof o confronti di enum.
     */
    public abstract void addTo(Player player);

    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

}