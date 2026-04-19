package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.buildingCards.CavePaintingEventBoostBC;
import org.example.server.model.cards.characters.Artist;
import org.example.server.model.cards.eventCards.CavePainting;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CavePaintingEventBoostBCTest {

    @Test
    @DisplayName("Zero artists: food remains unchanged after CavePainting event")
    void applyEvent_zeroArtists_initialFoodFive_foodRemainsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addFood(-playerWithBoost.getFood());
        playerWithBoost.addFood(5);
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(1, Era.I, 0, 0, BuildingCardType.CavePaintingEventBoostBC, false));

        CavePainting cavePainting = new CavePainting(100, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        cavePainting.applyEvent(match);

        assertEquals(5, playerWithBoost.getFood());
    }

    @Test
    @DisplayName("One artist: food increases from 0 to 1")
    void applyEvent_oneArtist_initialFoodZero_foodBecomesOne() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addFood(-playerWithBoost.getFood());
        playerWithBoost.addCharacter(new Artist(2, Era.I, CharacterType.ARTIST));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(3, Era.I, 0, 0, BuildingCardType.CavePaintingEventBoostBC, false));

        CavePainting cavePainting = new CavePainting(101, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        cavePainting.applyEvent(match);

        assertEquals(1, playerWithBoost.getFood());
    }

    @Test
    @DisplayName("Four artists: food increases from 2 to 6")
    void applyEvent_fourArtists_initialFoodTwo_foodBecomesSix() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addFood(-playerWithBoost.getFood());
        playerWithBoost.addFood(2);
        playerWithBoost.addCharacter(new Artist(4, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(5, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(6, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(7, Era.I, CharacterType.ARTIST));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(8, Era.I, 0, 0, BuildingCardType.CavePaintingEventBoostBC, false));

        CavePainting cavePainting = new CavePainting(102, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        cavePainting.applyEvent(match);

        assertEquals(6, playerWithBoost.getFood());
    }
}