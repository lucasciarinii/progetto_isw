package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class ShamanicRitual extends EventCard {

    public final int bonusPoints;
    public final int malusPoints;

    public ShamanicRitual(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("isEraFinal") boolean isEraFinal, @JsonProperty("eventEffect") EventEffect effect, @JsonProperty("bonusPoints") int bonusPoints, @JsonProperty("malusPoints") int malusPoints) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
    }

    public int getMalusPoints() {
        return malusPoints;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }
}
