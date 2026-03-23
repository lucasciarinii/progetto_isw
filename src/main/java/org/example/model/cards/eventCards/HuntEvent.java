package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

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
        super(id, era, isEraFinal, effect);
        this.points = points;
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
