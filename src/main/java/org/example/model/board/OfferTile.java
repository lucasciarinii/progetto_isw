package org.example.model.board;

import org.example.model.enums.OfferEffect;


public class OfferTile {

    private PlayerSlot slot;
    private final OfferEffect offerEffect;
    private final int MinPlayers;

    public OfferTile(OfferEffect offerEffect, int minPlayers) {
        this.slot = new PlayerSlot(""); // When initialize, there will be no player assigned to the slot, so we can set it to an empty string
        this.MinPlayers = minPlayers;
        this.offerEffect = offerEffect;
    }

    public PlayerSlot getSlot() {
        return this.slot;
    }

    public void setSlot(PlayerSlot slot) {
        this.slot = slot;
    }

    public OfferEffect getOfferEffect() {
        return this.offerEffect;
    }

    public int getMinPlayers() {
        return this.MinPlayers;
    }

}
