package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.buildingCards.SustenanceDiscountBC;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.List;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("points") int points
    ) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public void applyEvent(Match match) {

        //For each player, reset the sustenance discount,
        //apply all sustenance discount buildings, then pay the final food cost
        List<Player> players = match.getPlayers();

        for (Player player : players) {
            player.resetDiscountOnSustenance();

            for (BuildingCard building : player.getOwnedBuildings()) {
                if (building instanceof SustenanceDiscountBC) {
                    building.applyEffect(player, match);
                }
            }

            int finalFoodCost = points - player.getDiscountOnSustenance();

            //Food cost cannot go below zero
            if (finalFoodCost < 0) {
                finalFoodCost = 0;
            }

            player.addFood(-finalFoodCost);
        }
    }
}
