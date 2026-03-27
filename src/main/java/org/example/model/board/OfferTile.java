package org.example.model.board;

import org.example.model.enums.OfferEffect;
import org.example.model.match.Player;
import java.util.Objects;


public class OfferTile {

    private Player player;
    private final OfferEffect offerEffect;

    public OfferTile(OfferEffect offerEffect) {
        this.player = null;
        this.offerEffect = Objects.requireNonNull(offerEffect, "offerEffect cannot be null");
    }

    public Player getPlayer() {
        return player;
    }

    public OfferEffect getOfferEffect() {
        return this.offerEffect;
    }

    public void placePlayer(Player player) {

        Objects.requireNonNull(player, "player must not be null");

        if ( this.player != null ) {
            throw new IllegalArgumentException("tile already taken");
        }

        this.player = player;
    }


    public void removePlayer() {
        this.player = null;
    }


}
