package org.example.network.snapshots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.server.model.enums.OfferEffect;

import java.io.Serial;
import java.io.Serializable;

public class OfferTileSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final OfferEffect offerEffect;
    private final String occupantNickname;

    @JsonCreator
    public OfferTileSnapshot(
            @JsonProperty("offerEffect") OfferEffect offerEffect,
            @JsonProperty("occupantNickname") String occupantNickname) {
        this.offerEffect = offerEffect;
        this.occupantNickname = occupantNickname;
    }

    public OfferEffect getOfferEffect()   { return offerEffect; }
    public String getOccupantNickname()   { return occupantNickname; }
    @JsonIgnore
    public boolean isFree()               { return occupantNickname == null; }

    @Override
    public String toString() {
        return "[ %s, %s ] ".formatted((this.occupantNickname == null || this.occupantNickname.isBlank()) ? "empty" : this.occupantNickname.toUpperCase(), offerEffect);

    }
}
