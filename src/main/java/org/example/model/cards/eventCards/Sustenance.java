package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.buildingCards.SustenanceDiscountBC;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.model.cards.characters.Character;
import java.util.List;

public class Sustenance extends EventCard {

    private final int points;

    public Sustenance(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("isEraFinal") boolean isEraFinal, @JsonProperty("eventEffect") EventEffect effect, @JsonProperty("points") int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public void applyEvent(Match match) {
        List<Player> players = match.getPlayers();

        //For each player, count the number of characters they have and calculate the discount based on their SustenanceDiscountBC buildings.
        //Then, calculate the food they need to pay after applying the discount.
        //If they don't have enough food, they pay all their remaining food and lose points for each character that is not fed

        for (Player player : players) {
            int numCharacters = player.getCharacters().size();
            int discount = calculateDiscount(player);
            int foodToPay = numCharacters - discount;

            if (foodToPay < 0) {
                foodToPay = 0;
            }

            int availableFood = player.getFood();
            int paidFood;

            if (availableFood >= foodToPay) {
                paidFood = foodToPay;
            } else {
                paidFood = availableFood;
            }

            player.addFood(-paidFood);

            int notFedCharacters = numCharacters - discount - paidFood;
            if (notFedCharacters < 0) {
                notFedCharacters = 0;
            }

            int lostPoints = notFedCharacters * points;
            player.addPoints(lostPoints);
        }
    }

    private int calculateDiscount(Player player) {
        int discount = 0;

        for (int i = 0; i < player.getOwnedBuildings().size(); i++) {
            BuildingCard building = player.getOwnedBuildings().get(i);

            if (building.getClassType() == BuildingCardType.SustenanceDiscountBC) {
                SustenanceDiscountBC discountBC = (SustenanceDiscountBC) building;

                for (int j = 0; j < player.getCharacters().size(); j++) {
                    Character character = player.getCharacters().get(j);

                    if (character.getCharacterType() == discountBC.getCharacterEffect()) {
                        discount++;
                    }
                }
            }
        }

        return discount;
    }
}
