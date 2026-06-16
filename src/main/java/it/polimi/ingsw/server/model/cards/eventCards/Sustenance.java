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
 * Sustenance event that requires players to pay food or points.
 */
public class Sustenance extends EventCard {

    /** Points paid for each unpaid character. */
    private final int points;

    /**
     * Creates the event card from JSON data.
     *
     * @param id card id
     * @param era card era
     * @param isEraFinal true if it ends the era
     * @param effect event effect type
     * @param points points paid per character
     */
    @SuppressWarnings("unused")
    public Sustenance(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("points") int points
    ) {
        super(id, era, isEraFinal, EventEffect.SUSTENANCE);
        this.points = points;
    }

    @Override
    public String toString() {
        return "%s\tpay 1 food for each character card OR\n\tpay %d points for it%s\n".formatted(super.toString(), points, ConsoleColors.RESET);
    }

    /**
     * @return true because this is the sustenance event
     */
    @Override
    public boolean isSustenance() { return true; }

    /**
     * Applies sustenance costs and related discount buildings.
     *
     * @param match current match
     */
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