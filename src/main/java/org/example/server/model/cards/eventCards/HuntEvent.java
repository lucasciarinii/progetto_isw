package org.example.server.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.Objects;

public class HuntEvent extends EventCard {

    private final int points;
    private final static int FOOD_BONUS = 1;

    public HuntEvent(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("points") int points
    ) {
        super(id, era, isEraFinal, EventEffect.HUNT_EVENT);
        this.points = points;
    }

    @Override
    public String toString() {
        return "%s\t%d food + %d points X number of hunters%s\n".formatted(super.toString(), FOOD_BONUS, this.points, ConsoleColors.RESET);
    }

    @Override
    public void applyEvent(Match match) {

        Objects.requireNonNull(match, "Match cannot be null");

        //For each player, count the number of hunters they own
        //and award the standard Hunt event rewards
        for (Player player : match.getPlayers()) {
            int hunters = player.getHunters().size();

            int gainedPoints = hunters * points;
            player.addPoints(gainedPoints);

            player.addFood(FOOD_BONUS);

            //Apply all Hunt event boost buildings owned by the player
            for (BuildingCard building : player.getOwnedBuildings()) {
                if (building.getClassType() == BuildingCardType.HuntEventBoostBC) {
                    building.applyEffect(player, match);
                }
            }
        }
    }
}
