package org.example.model.cards.eventCards;

import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.model.cards.characters.Character;
import org.example.model.enums.CharacterType;
import java.util.List;


public class HuntEvent extends EventCard {

    private final int points;

    public HuntEvent(int id, Era era, boolean isEraFinal, EventEffect effect, int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public void applyEvent(Match match) {

        //For each player,
        //count the number of hunters they have and give them 1 food
        // and points equal to the number of hunters multiplied by the points value of the card

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