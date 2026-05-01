package org.example.network.Snapshots;

import org.example.server.model.enums.OfferEffect;

import java.io.Serial;
import java.io.Serializable;

public class OfferTileSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final OfferEffect offerEffect;
    private final String occupantNickname;

    public OfferTileSnapshot(OfferEffect offerEffect, String occupantNickname) {
        this.offerEffect = offerEffect;
        this.occupantNickname = occupantNickname;
    }

    public OfferEffect getOfferEffect()   { return offerEffect; }
    public String getOccupantNickname()   { return occupantNickname; }
    public boolean isFree()               { return occupantNickname == null; }

    @Override
    public String toString() {
        return "[ %s, %s ] ".formatted((this.occupantNickname == null || this.occupantNickname.isBlank()) ? "empty" : this.occupantNickname.toUpperCase(), offerEffect);

    }
}
