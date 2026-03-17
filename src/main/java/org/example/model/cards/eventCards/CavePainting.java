package org.example.model.cards.eventCards;

import org.example.model.cards.characters.Character;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Context;
import org.example.model.match.Player;

import java.util.List;

public class CavePainting extends EventCard {

    private final int bonusPoints;
    private final int malusPoints;
    private final int interval;

    public CavePainting(int id, Era era, boolean isEraFinal, EventEffect effect, int bonusPoints, int malusPoints, int interval) {
        super(id, era, isEraFinal, effect);
        this.bonusPoints = bonusPoints;
        this.malusPoints = malusPoints;
        this.interval = interval;
    }

    public void applyEvent(Context context) {

        //For each player count the number of artists they have
        //and give them points equal to the number of artists multiplied by the points value of the card.
        //If a player has fewer artists than the interval value, he loses points equal to the malus points value of the card.

        List<Player> players = context.getPlayers();
        for (Player player : players) {
            int artists = 0;

            for (int j = 0; j < player.getOwnedCharacters().size(); j++) {
                Character character = player.getOwnedCharacters().get(j);

                if (character.getCharacterType() == CharacterType.ARTIST) {
                    artists++;
                }
            }

            if (artists < interval) {
                player.addPoints(malusPoints);
            } else {
                player.addPoints(artists * bonusPoints);
            }
        }
    }
}