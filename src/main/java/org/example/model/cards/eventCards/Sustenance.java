package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.Objects;

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

    @Override
    public void applyEvent(Match match) {

        Objects.requireNonNull(match, "Match cannot be null");

        //apply all sustenance discount buildings, then pay the final food cost
        for (Player player : match.getPlayers()) {

            for (BuildingCard building : player.getOwnedBuildings()) {
                if (building.getClassType() == BuildingCardType.SustenanceDiscountBC) {
                    building.applyEffect(player, match);
                }
            }

            int totalCharacters = player.getInventors().size() + player.getGatherers().size() + player.getShamans().size() +
                    player.getBuilders().size() + player.getArtists().size() + player.getHunters().size();

            int totalCharacterToPay = totalCharacters - player.getDiscountOnSustenance();

            if(totalCharacterToPay < 0) {
                totalCharacterToPay = 0;
            }


            int remainingCharacters = player.getFood() - totalCharacterToPay;

            if ( remainingCharacters < 0 ) {
                player.addFood( - player.getFood());
                player.addPoints( (remainingCharacters) * points);
            }

            else {
                player.addFood( - totalCharacterToPay);
            }


        }
        }
    }
