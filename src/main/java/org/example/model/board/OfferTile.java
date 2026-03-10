package org.example.model.board;
import  org.example.model.board.PlayerSlot;
import  org.example.model.enums.OfferEffect;


public class OfferTile {

    private PlayerSlot slot;
    private OfferEffect offerEffect;
    private int MinPlayers;

    public OfferTile(OfferEffect offerEffect,  int minPlayers) {
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
