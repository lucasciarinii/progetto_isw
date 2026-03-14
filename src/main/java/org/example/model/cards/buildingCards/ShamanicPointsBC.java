package org.example.model.cards.buildingCards;

import org.example.model.board.Board;
import org.example.model.enums.Era;
import org.example.model.match.Player;

import java.util.ArrayList;

public class ShamanicPointsBC extends BuildingCard {

    private final boolean shouldDoublePrestigePoints;

    public ShamanicPointsBC(int id, Era era, int foodCost, int endPoints, boolean isEndGame, boolean type) {
        super(id, era, foodCost, endPoints, isEndGame);
        this.shouldDoublePrestigePoints = type;
    }

    @Override
    public void applyEffect(Player owner, Board board) {
        throw new UnsupportedOperationException();
    }


    private boolean applyShamanicRitualEffect(Player owner, ArrayList<Player> players) {


        // During the Shamanic Ritual, if it's the first type of card, if the player has more (or the same number)
        // Shamanic Stars than the other players, he earns double of Prestige Points.
        if ( !shouldDoublePrestigePoints) {

            for (Player p : players) {

                // skip cycle if the player in the List is the owner
                if ( p.equals(owner) ) {
                    continue;
                }

                // check if the player in the List has more Shamanic Stars than the owner of the building, it returns
                // false to the controller, and it doesn't apply the effect
                if ( p.getShamanStars() > owner.getShamanStars() ) {
                    return false;
                }
                
            }

        }

        // During a Shamanic Ritual, if it's the second type of card, if the player has less Shamanic Stars than any
        // other player, he doesn't lose Prestige Points
        else {

            for (Player p : players ) {

                // skip cycle if the player in the List is the owner
                if ( p.equals(owner) ) {
                    continue;
                }

                // check if the player in the list has less Shamanic Stars than the owner of the building, it returns
                // false to the controller, and it doesn't apply the effect
                if ( p.getShamanStars() < owner.getShamanStars() ) {
                    return false;
                }

            }

        }
        return true;

    }

}