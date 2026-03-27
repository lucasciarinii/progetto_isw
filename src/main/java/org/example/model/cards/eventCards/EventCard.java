package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.cards.Card;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import java.util.Objects;

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

    private final EventEffect eventEffect;
    private final boolean isEraFinal;

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

    public EventEffect getEventEffect() {
        return eventEffect;
    }

    public boolean isEraFinal() {
        return isEraFinal;
    }

    public abstract void applyEvent(Match match);
}
