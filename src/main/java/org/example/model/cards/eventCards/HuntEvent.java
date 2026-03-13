package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;

public class HuntEvent extends EventCard {

    private final int points;

    public HuntEvent(int id, Era era, boolean isEraFinal, EventEffect effect, int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints() {
        return points;
    }


}
