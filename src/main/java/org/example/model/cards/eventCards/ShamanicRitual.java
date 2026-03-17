package org.example.model.cards.eventCards;

import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.buildingCards.ShamanicPointsBC;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Context;
import org.example.model.match.Player;

import java.util.Collections;
import java.util.List;

public class ShamanicRitual extends EventCard {

    public final int bonusPoints;
    public final int malusPoints;

    public ShamanicRitual(int id, Era era, boolean isEraFinal, EventEffect effect, int bonusPoints, int malusPoints) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
    }

    public void applyEvent(Context c) {

        //Find the max and min number of stars among the players

        List<Player> players = c.getPlayers();

        int minStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);


        int maxStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);

        //Add or remove points to the players based on the number of stars they have
        //and the presence of the Shamanic Points building card

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int stars = player.getShamanStars();

            if (stars == maxStars) {
                int gainedPoints = bonusPoints;

                if (hasShamanicPointsBuilding(player, true) && isUniqueMaximum(player, players)) {
                    gainedPoints = bonusPoints * 2;
                }

                player.addPoints(gainedPoints);
            }

            if (stars == minStars) {
                if (!hasShamanicPointsBuilding(player, false)) {
                    player.addPoints(malusPoints);
                }
            }
        }
    }

    //Check if the player has the Shamanic Points building card and if it has the expected value depending on
    //the doubling points card and the noMalus card

    private boolean hasShamanicPointsBuilding(Player player, boolean expectedValue) {
        for (int i = 0; i < player.getOwnedBuildings().size(); i++) {
            BuildingCard building = player.getOwnedBuildings().get(i);

            if (building.getClassType() == BuildingCardType.ShamanicPointsBC) {
                ShamanicPointsBC spbc = (ShamanicPointsBC) building;

                if (spbc.shouldDoublePrestigePoints() == expectedValue) {
                    return true;
                }
            }
        }

        return false;
    }

    //Check if the player is the only one with the maximum number of stars

    private boolean isUniqueMaximum(Player owner, List<Player> players) {
        for (Player p : players) {
            if (!p.equals(owner) && p.getShamanStars() == owner.getShamanStars()) {
                return false;
            }
        }

        return true;
    }
}