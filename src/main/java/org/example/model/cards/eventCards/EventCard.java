package org.example.model.cards.eventCards;

import org.example.model.cards.Card;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;


public abstract class EventCard extends Card {


    private final EventEffect eventEffect;


    public EventCard(Era era, boolean isEraFinal, EventEffect effect) {
        super(era);
        this.eventEffect = effect;
    }


    public EventEffect getEventEffect() {
        return eventEffect;
    }
}