package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class CavePainting extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;
    private final int interval;

    public CavePainting(Era era, boolean isEraFinal, EventEffect effect, int bonusPoints, int malusPoints, int interval) {
        super(era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
        this.interval = interval;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public int getMalusPoints() {
        return malusPoints;
    }

    public int getInterval() {
        return interval;
    }
}
