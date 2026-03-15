package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Player;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("isEraFinal") boolean isEraFinal, @JsonProperty("eventEffect") EventEffect effect, @JsonProperty("points") int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints(Player player) {
        int numCharachters = player.getOwnedCharacters().size();

        int availableFood = player.getFood();
        int paidFood;

        //Check if I can feed all characters
        if (availableFood >= numCharachters) {
            paidFood = numCharachters;
        } else {
            paidFood = availableFood;
        }

        //Remove food used
        player.addFood(-paidFood);

        //Calculate point lost depending on notFedCharacters number
        int notFedCharacters = numCharachters - paidFood;
        int lostPoints = notFedCharacters * points;

        return lostPoints;
    }
}
