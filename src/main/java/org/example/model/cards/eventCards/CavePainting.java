package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class CavePainting extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;
    private final int delta;

    public CavePainting(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("isEraFinal") boolean isEraFinal, @JsonProperty("eventEffect") EventEffect effect, @JsonProperty("bonusPoints") int bonusPoints, @JsonProperty("malusPoints") int malusPoints, @JsonProperty("delta") int delta) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
        this.delta = delta;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public int getMalusPoints() {
        return malusPoints;
    }

    public int getDelta() {
        return delta;
    }
}
