package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.buildingCards.ShamanicPointsBC;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.List;

public class ShamanicRitual extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;

    public ShamanicRitual(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("bonusPoints") int bonusPoints,
            @JsonProperty("malusPoints") int malusPoints
    ) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public int getMalusPoints() {
        return malusPoints;
    }

    @Override
    public void applyEvent(Match match) {

        //Find the maximum and minimum number of shaman stars among all players
        List<Player> players = match.getPlayers();

        int minStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .min()
                .orElse(0);

        int maxStars = players.stream()
                .mapToInt(Player::getShamanStars)
                .max()
                .orElse(0);

        //Add or remove prestige points based on the number of shaman stars
        //and on the presence of ShamanicPointsBC
        for (Player player : players) {
            int stars = player.getShamanStars();

            if (stars == maxStars) {
                int gainedPoints = bonusPoints;

                //Double points only if the player has the doubling building
                //and is the unique player with the maximum number of stars
                if (hasShamanicPointsBuilding(player, true) && isUniqueMaximum(player, players)) {
                    gainedPoints = bonusPoints * 2;
                }

                player.addPoints(gainedPoints);
            }

            if (stars == minStars) {

                //Do not lose points if the player has the protection building
                if (!hasShamanicPointsBuilding(player, false)) {
                    player.addPoints(malusPoints);
                }
            }
        }
    }

    //Check if the player has a ShamanicPointsBC with the expected behavior:
    //true = double prestige points, false = protection from malus
    private boolean hasShamanicPointsBuilding(Player player, boolean expectedValue) {
        for (BuildingCard building : player.getOwnedBuildings()) {
            if (building.getClassType() == BuildingCardType.ShamanicPointsBC) {
                ShamanicPointsBC shamanicPointsBC = (ShamanicPointsBC) building;

                if (shamanicPointsBC.shouldDoublePrestigePoints() == expectedValue) {
                    return true;
                }
            }
        }

        return false;
    }

    //Check if the player is the only one with the maximum number of stars
    private boolean isUniqueMaximum(Player owner, List<Player> players) {
        for (Player player : players) {
            if (!player.equals(owner) && player.getShamanStars() == owner.getShamanStars()) {
                return false;
            }
        }

        return true;
    }
}
