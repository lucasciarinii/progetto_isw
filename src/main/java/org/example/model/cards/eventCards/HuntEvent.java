package org.example.model.cards.eventCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.cards.characters.Character;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.List;

public class HuntEvent extends EventCard {

    private final int points;

    public HuntEvent(
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

        //For each player, count the number of hunters they own
        //and award food and prestige points accordingly
        List<Player> players = match.getPlayers();

        for (Player player : players) {
            int hunters = 0;

            for (int j = 0; j < player.getOwnedCharacters().size(); j++) {
                Character character = player.getOwnedCharacters().get(j);

                if (character.getCharacterType() == CharacterType.HUNTER) {
                    hunters++;
                }
            }

            int gainedPoints = hunters * points;
            player.addFood(1);
            player.addPoints(gainedPoints);
        }
    }
}
