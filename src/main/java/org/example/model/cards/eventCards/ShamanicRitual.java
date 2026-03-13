package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class ShamanicRitual extends EventCard {

    public final int bonusPoints;
    public final int malusPoints;

    public ShamanicRitual(int id, Era era, boolean isEraFinal, EventEffect effect, int bonusPoints, int malusPoints) {
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
