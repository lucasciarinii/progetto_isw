package org.example.model.cards.eventCards;

import org.example.model.cards.characters.Character;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Player;


public class HuntEvent extends EventCard {

    private final int points;

    public HuntEvent(int id, Era era, boolean isEraFinal, EventEffect effect, int points) {
        super(id, era, isEraFinal, effect);
        this.points = points;
    }

    public int getPoints(Player player) {


        int hunters = 0;

        for (int i = 0;  i < player.getOwnedCharacters().size(); i++) {
                Character c = player.getOwnedCharacters().get(i);
                if(c.getCharacterType() == CharacterType.HUNTER) {hunters++;}


        }
        int gainedPoints = hunters * points;
        player.addFood(1);

        return gainedPoints;
    }


}
