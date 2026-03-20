package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Character;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.List;

public class CavePainting extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;
    private final int interval;

    public CavePainting(
            @JsonProperty("id") int id,
            @JsonProperty("era") Era era,
            @JsonProperty("isEraFinal") boolean isEraFinal,
            @JsonProperty("eventEffect") EventEffect effect,
            @JsonProperty("bonusPoints") int bonusPoints,
            @JsonProperty("malusPoints") int malusPoints,
            @JsonProperty("interval") int interval
    ) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
        this.interval = interval;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public int getMalusPoints() {
        return malusPoints;
    }

    public int getInterval() {
        return interval;
    }

    @Override
    public void applyEvent(Match match) {

        //For each player, count the number of artists they own
        //and assign points according to the event rules
        List<Player> players = match.getPlayers();

        for (Player player : players) {
            int artists = 0;

            for (int j = 0; j < player.getOwnedCharacters().size(); j++) {
                Character character = player.getOwnedCharacters().get(j);

                if (character.getCharacterType() == CharacterType.ARTIST) {
                    artists++;
                }
            }

            //If the player has fewer artists than the required interval,
            //apply the malus; otherwise award bonus points
            if (artists < interval) {
                player.addPoints(malusPoints);
            } else {
                player.addPoints(artists * bonusPoints);
            }
        }
    }
}
