package it.polimi.ingsw.server.model.cards.eventCards;

import it.polimi.ingsw.server.model.cards.buildingCards.CavePaintingEventBoostBC;
import it.polimi.ingsw.server.model.cards.characters.Artist;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.EventEffect;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CavePaintingTest {

    // Tets: applyEvent with null match should throw NullPointerException
    @Test
    @DisplayName("applyEvent(null) throws NullPointerException")
    void applyEvent_nullMatch_throwsNullPointerException() {
        CavePainting cavePainting = new CavePainting(1, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -1, 2);

        assertThrows(NullPointerException.class, () -> cavePainting.applyEvent(null));
    }

    // Test: applyEvent with empty players list should not throw and should do nothing
    @Test
    @DisplayName("applyEvent does nothing when the match has no players")
    void applyEvent_emptyPlayersList_doesNothing() {
        CavePainting cavePainting = new CavePainting(1, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -1, 2);

        Match emptyPlayersMatch = new Match(List.of(new Player("Alice"), new Player("Bob"))) {
            @Override
            public List<Player> getPlayers() {
                return List.of();
            }
        };

        assertDoesNotThrow(() -> cavePainting.applyEvent(emptyPlayersMatch));
    }

    // Test: a player with 0 artists and no buildings receives the malus points
    @Test
    @DisplayName("applyEvent gives the malus when the player has fewer artists than the interval")
    void applyEvent_playerWithNoArtistsGetsMalusPoints() {
        CavePainting cavePainting = new CavePainting(1, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        // Situation: Bob has 0 artists, Charlie has 2 artists, interval is 2, so Bob receives the malus points and Charlie receives the bonus points
        match.getPlayers().get(1).addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
        match.getPlayers().get(1).addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));

        int bobPointsBefore = match.getPlayers().get(0).getPoints(); // Bob's points before the event
        int charliePointsBefore = match.getPlayers().get(1).getPoints(); // Charlie's points before the event

        cavePainting.applyEvent(match);

        assertEquals(bobPointsBefore -3, match.getPlayers().get(0).getPoints());
        assertEquals(charliePointsBefore + (2*2), match.getPlayers().get(1).getPoints());
    }

    // Test: with 1 artist and interval 2, the player still receives a fixed malus
    @Test
    @DisplayName("applyEvent applies fixed malus and no food change when artists are below interval")
    void applyEvent_playerWithOneArtistGetsFixedMalusAndNoFoodChange() {
        CavePainting cavePainting = new CavePainting(2, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        Player malusPlayer = match.getPlayers().get(0);
        Player otherPlayer = match.getPlayers().get(1);

        // Malus player has exactly 1 artist, which is below interval 2.
        malusPlayer.addCharacter(new Artist(2000, Era.I, CharacterType.ARTIST));
        // The second player is set on the bonus branch to keep the scenario explicit.
        otherPlayer.addCharacter(new Artist(2001, Era.I, CharacterType.ARTIST));
        otherPlayer.addCharacter(new Artist(2002, Era.I, CharacterType.ARTIST));

        int malusPlayerPointsBefore = malusPlayer.getPoints();
        int malusPlayerFoodBefore = malusPlayer.getFood();

        cavePainting.applyEvent(match);

        assertEquals(malusPlayerPointsBefore - 3, malusPlayer.getPoints());
        assertEquals(malusPlayerFoodBefore, malusPlayer.getFood());
    }

    // Test: with 0 artists and one CavePaintingEventBoost building, food boost is still 0.
    @Test
    @DisplayName("applyEvent with CavePainting boost building gives fixed malus and no food gain with zero artists")
    void applyEvent_playerWithNoArtistsAndCavePaintingBoostBuilding_getsMalusAndNoFoodChange() {
        CavePainting cavePainting = new CavePainting(3, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(
                3000,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));

        int pointsBefore = playerWithBoost.getPoints();
        int foodBefore = playerWithBoost.getFood();

        cavePainting.applyEvent(match);

        assertEquals(pointsBefore - 3, playerWithBoost.getPoints());
        assertEquals(foodBefore, playerWithBoost.getFood());
    }

    // Test: a player with 1 artist and a CavePaintingEventBoost building gets fixed malus points and +1 food
    @Test
    @DisplayName("applyEvent with CavePainting boost building gives fixed malus and +1 food with one artist")
    void applyEvent_playerWithOneArtistAndCavePaintingBoostBuilding_getsMalusAndOneFood() {
        CavePainting cavePainting = new CavePainting(4, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        Player playerWithBoost = match.getPlayers().get(0);

        // The player has exactly 1 artist, which is below the interval, and owns one boost building.
        playerWithBoost.addCharacter(new Artist(4000, Era.I, CharacterType.ARTIST));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(
                4001,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));

        int pointsBefore = playerWithBoost.getPoints();
        int foodBefore = playerWithBoost.getFood();

        cavePainting.applyEvent(match);

        assertEquals(pointsBefore - 3, playerWithBoost.getPoints());
        assertEquals(foodBefore + 1, playerWithBoost.getFood());
    }

    // Test: with 3 artists and one CavePaintingEventBoost building, the player gets bonus points and 3 food
    @Test
    @DisplayName("applyEvent with CavePainting boost building gives +6 points and +3 food with three artists")
    void applyEvent_playerWithThreeArtistsAndOneCavePaintingBoostBuilding_getsSixPointsAndThreeFood() {
        CavePainting cavePainting = new CavePainting(5, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addCharacter(new Artist(5000, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(5001, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(5002, Era.I, CharacterType.ARTIST));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(
                5003,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));

        int pointsBefore = playerWithBoost.getPoints();
        int foodBefore = playerWithBoost.getFood();

        cavePainting.applyEvent(match);

        assertEquals(pointsBefore + 6, playerWithBoost.getPoints());
        assertEquals(foodBefore + 3, playerWithBoost.getFood());
    }

    // Test: with 3 artists and two CavePaintingEventBoost buildings, the player gets bonus points and 6 food
    @Test
    @DisplayName("applyEvent with two CavePainting boost buildings gives +6 points and +6 food with three artists")
    void applyEvent_playerWithThreeArtistsAndTwoCavePaintingBoostBuildings_getsSixPointsAndSixFood() {
        CavePainting cavePainting = new CavePainting(6, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(new Player("Bob"), new Player("Charlie")));
        Player playerWithBoost = match.getPlayers().get(0);

        playerWithBoost.addCharacter(new Artist(6000, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(6001, Era.I, CharacterType.ARTIST));
        playerWithBoost.addCharacter(new Artist(6002, Era.I, CharacterType.ARTIST));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(
                6003,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));
        playerWithBoost.addBuilding(new CavePaintingEventBoostBC(
                6004,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));

        int pointsBefore = playerWithBoost.getPoints();
        int foodBefore = playerWithBoost.getFood();

        cavePainting.applyEvent(match);

        assertEquals(pointsBefore + 6, playerWithBoost.getPoints());
        assertEquals(foodBefore + 6, playerWithBoost.getFood());
    }

    // Test: the event is resolved independently for three players in the same match
    @Test
    @DisplayName("applyEvent resolves the correct points and food for multiple players")
    void applyEvent_threePlayersResolvedIndependently() {
        CavePainting cavePainting = new CavePainting(7, Era.I, false, EventEffect.CAVE_PAINTINGS, 2, -3, 2);

        Match match = new Match(List.of(
                new Player("A"),
                new Player("B"),
                new Player("C")
        ));

        Player playerA = match.getPlayers().stream()
                .filter(player -> player.getNickname().equals("A"))
                .findFirst()
                .orElseThrow();
        Player playerB = match.getPlayers().stream()
                .filter(player -> player.getNickname().equals("B"))
                .findFirst()
                .orElseThrow();
        Player playerC = match.getPlayers().stream()
                .filter(player -> player.getNickname().equals("C"))
                .findFirst()
                .orElseThrow();

        // Player A: 0 artists, no buildings
        // Player B: 2 artists, no buildings
        // Player C: 3 artists, one boost building
        playerB.addCharacter(new Artist(7000, Era.I, CharacterType.ARTIST));
        playerB.addCharacter(new Artist(7001, Era.I, CharacterType.ARTIST));

        playerC.addCharacter(new Artist(7002, Era.I, CharacterType.ARTIST));
        playerC.addCharacter(new Artist(7003, Era.I, CharacterType.ARTIST));
        playerC.addCharacter(new Artist(7004, Era.I, CharacterType.ARTIST));
        playerC.addBuilding(new CavePaintingEventBoostBC(
                7005,
                Era.I,
                0,
                0,
                BuildingCardType.CavePaintingEventBoostBC,
                false
        ));

        int playerAPointsBefore = playerA.getPoints();
        int playerAFoodBefore = playerA.getFood();
        int playerBPointsBefore = playerB.getPoints();
        int playerBFoodBefore = playerB.getFood();
        int playerCPointsBefore = playerC.getPoints();
        int playerCFoodBefore = playerC.getFood();

        cavePainting.applyEvent(match);

        assertEquals(playerAPointsBefore - 3, playerA.getPoints());
        assertEquals(playerAFoodBefore, playerA.getFood());

        assertEquals(playerBPointsBefore + 4, playerB.getPoints());
        assertEquals(playerBFoodBefore, playerB.getFood());

        assertEquals(playerCPointsBefore + 6, playerC.getPoints());
        assertEquals(playerCFoodBefore + 3, playerC.getFood());
    }


}