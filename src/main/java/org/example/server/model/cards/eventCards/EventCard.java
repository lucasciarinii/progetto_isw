package org.example.server.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.cards.Card;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;

/**
 * Base class for event cards resolved during game.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventEffect"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HuntEvent.class, name = "HUNT-EVENT"),
        @JsonSubTypes.Type(value = CavePainting.class, name = "CAVE_PAINTING"),
        @JsonSubTypes.Type(value = ShamanicRitual.class, name = "SHAMANIC_RITUAL"),
        @JsonSubTypes.Type(value = Sustenance.class, name = "SUSTENANCE"),
})
public abstract class EventCard extends Card {

    /** Event effect type. */
    private final EventEffect eventEffect;
    /** True if this event closes the current era. */
    private final boolean isEraFinal;

    /**
     * Creates an event card.
     *
     * @param id card id
     * @param era card era
     * @param isEraFinal true if it ends the era
     * @param effect event effect type
     */
    public EventCard(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect
    ) {
        super(id, era);
        this.isEraFinal = isEraFinal;
        this.eventEffect = effect;
    }

    @Override
    public String toString() {
        return "%s%s [id: %d] {ERA %s}\n".formatted(ConsoleColors.BROWN, this.eventEffect, this.getId(), getEra());
    }

    /**
     * @return true because this is an event card
     */
    @Override
    public boolean isEventCard() { return true; }

    /**
     * @return event effect type
     */
    public EventEffect getEventEffect() {
        return eventEffect;
    }

    /**
     * @return true if this event ends the era
     */
    public boolean isEraFinal() {
        return isEraFinal;
    }

    /**
     * Applies the event to the given match.
     *
     * @param match current match
     */
    public abstract void applyEvent(Match match);
}
