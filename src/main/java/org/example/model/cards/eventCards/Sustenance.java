package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(Era era, boolean isEraFinal, EventEffect effect, int points) {
        super(era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
