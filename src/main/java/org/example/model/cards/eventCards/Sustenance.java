package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Player;
import org.example.model.match.Context;
import java.util.List;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("isEraFinal") boolean isEraFinal, @JsonProperty("eventEffect") EventEffect effect, @JsonProperty("points") int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public void applyEvent(Context c) {

        //For each player use 1 food to feed each one of his characters.
        // If a player cannot feed all his characters,
        // he loses points equal to the number of unfed characters multiplied by the points value of the card.

        List<Player> players = c.getPlayers();
        for (Player player : players) {
            int numCharacters = player.getOwnedCharacters().size();
            int availableFood = player.getFood();
            int paidFood;

            if (availableFood >= numCharacters) {
                paidFood = numCharacters;
            } else {
                paidFood = availableFood;
            }

            player.addFood(-paidFood);

            int notFedCharacters = numCharacters - paidFood;
            int lostPoints = notFedCharacters * points;

            player.addPoints(-lostPoints);
        }
    }
}