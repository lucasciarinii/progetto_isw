package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(int id, Era era, boolean isEraFinal, EventEffect effect, int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
