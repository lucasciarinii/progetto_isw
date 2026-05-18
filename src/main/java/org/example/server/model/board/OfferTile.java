package org.example.server.model.board;

import org.example.server.model.enums.OfferEffect;
import org.example.server.model.match.Player;
import java.util.Objects;


/**
 * Tile on the offer track that may host a player and an effect.
 */
public class OfferTile {

    /** Player currently occupying the tile, or null if empty. */
    private Player player;
    /** Effect provided by this tile. */
    private final OfferEffect offerEffect;

    /**
     * Creates an offer tile with the given effect.
     *
     * @param offerEffect offer effect on this tile
     */
    public OfferTile(OfferEffect offerEffect) {
        this.player = null;
        this.offerEffect = Objects.requireNonNull(offerEffect, "offerEffect cannot be null");
    }

    /**
     * @return the player occupying the tile, or null
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the effect associated with the tile
     */
    public OfferEffect getOfferEffect() {
        return this.offerEffect;
    }

    /**
     * Places a player on the tile.
     *
     * @param player player to place
     */
    public void placePlayer(Player player) {

        if ( this.player != null ) {
            throw new IllegalArgumentException("tile already taken");
        }

        this.player = player;
    }


    /**
     * Removes the player from the tile.
     */
    public void removePlayer() {
        this.player = null;
    }


}
