package it.polimi.ingsw.server.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.client.view.TUI.ConsoleColors;
import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.EventEffect;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

import java.util.Objects;

/**
 * Hunt event that grants food and points per hunter.
 */
public class HuntEvent extends EventCard {

    /** Points awarded per hunter. */
    private final int points;
    /** Base food bonus for the event. */
    private static final int FOOD_BONUS = 1;

    /**
     * Creates the event card from JSON data.
     *
     * @param id card id
     * @param era card era
     * @param isEraFinal true if it ends the era
     * @param effect event effect type
     * @param points points per hunter
     */
    @SuppressWarnings("unused")
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

    /**
     * Applies hunt rewards and any hunt-boost buildings.
     *
     * @param match current match
     */
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
