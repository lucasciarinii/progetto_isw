package org.example.server.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.tui.ConsoleColors;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.Objects;

public class CavePainting extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;
    private final int interval;

    public CavePainting(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("bonusPoints") int bonusPoints,
            @JsonProperty("malusPoints") int malusPoints,
            @JsonProperty("interval") int interval
    ) {
        super(id, era, isEraFinal, EventEffect.CAVE_PAINTINGS);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "%s\t<%d artists: %d points\n\t>=%d artists: %d points X number of artists%s\n".formatted(super.toString(), interval, malusPoints, interval, bonusPoints, ConsoleColors.RESET);
    }

    @Override
    public void applyEvent(Match match) {

        Objects.requireNonNull(match, "Match cannot be null when applying Cave Painting event");

        //For each player, count the number of artists they own
        //and assign points according to the event rules
        for (Player player : match.getPlayers()) {
            int artists = player.getArtists().size();

            //If the player has fewer artists than the required interval,
            //apply the malus; otherwise award bonus points
            if (artists < interval) {
                player.addPoints(malusPoints);
            } else {
                player.addPoints(artists * bonusPoints);
            }

            //Apply all Cave Painting event boost buildings owned by the player
            for (BuildingCard building : player.getOwnedBuildings()) {
                if (building.getClassType() == BuildingCardType.CavePaintingEventBoostBC) {
                    building.applyEffect(player, match);
                }
            }
        }
    }
}
