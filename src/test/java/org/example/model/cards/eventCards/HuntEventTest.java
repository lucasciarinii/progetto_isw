package org.example.model.cards.eventCards;

import org.example.model.cards.characters.Hunter;
import org.example.model.cards.buildingCards.HuntEventBoostBC;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HuntEventTest {

    //? STUB: A stub Match class that returns an empty list of players, to test the behavior of applyEvent when there are no players in the match.
    private static class EmptyPlayersMatch extends Match {
        EmptyPlayersMatch() {
            super(java.util.List.of(new Player("p1"), new Player("p2")));
        }

        @Override
        public java.util.List<Player> getPlayers() {
            return java.util.List.of();
        }
    }

    //* TEST CASES
    // Test that applyEvent(null) throws NullPointerException, because there is Objects.requireNonNull(match, ...).
    @Test
    void applyEvent_nullMatch_throwsNullPointerException() {
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);

        assertThrows(NullPointerException.class, () -> huntEvent.applyEvent(null));
    }

    // Test that applyEvent with a match that has no players does not throw an exception.
    @Test
    void applyEvent_emptyPlayers_doesNotThrow() {
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);
        Match emptyPlayersMatch = new EmptyPlayersMatch();

        assertDoesNotThrow(() -> huntEvent.applyEvent(emptyPlayersMatch));
    }

    // Test with: 0 hunters and no related buildings: points unchanged, basic food +1
    @Test
    void applyEvent_playerWithNoHuntersAndNoBuildings_getsOnlyBaseFood() {
        Match match = new Match(java.util.List.of(new Player("p1"), new Player("p2")));
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 3);

        Player monitoredPlayer = match.getPlayers().get(0);
        assertTrue(monitoredPlayer.getHunters().isEmpty());
        assertTrue(monitoredPlayer.getOwnedBuildings().isEmpty());

        int beforePoints = monitoredPlayer.getPoints();
        int beforeFood = monitoredPlayer.getFood();

        huntEvent.applyEvent(match);

        assertEquals(beforePoints, monitoredPlayer.getPoints());
        assertEquals(beforeFood + 1, monitoredPlayer.getFood());
    }

    // Test with 3 hunters and no related buildings: points +6 (3*2), basic food +1
    @Test
    void applyEvent_playerWithThreeHuntersAndNoBuildings_getsSixPointsAndBaseFood() {
        Match match = new Match(java.util.List.of(new Player("p1"), new Player("p2")));
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);

        Player monitoredPlayer = match.getPlayers().get(0);
        assertTrue(monitoredPlayer.getOwnedBuildings().isEmpty());

        monitoredPlayer.addCharacter(new Hunter(1001, Era.I, CharacterType.HUNTER, false));
        monitoredPlayer.addCharacter(new Hunter(1002, Era.I, CharacterType.HUNTER, false));
        monitoredPlayer.addCharacter(new Hunter(1003, Era.I, CharacterType.HUNTER, false));
        assertEquals(3, monitoredPlayer.getHunters().size());

        int beforePoints = monitoredPlayer.getPoints();
        int beforeFood = monitoredPlayer.getFood();

        huntEvent.applyEvent(match);

        assertEquals(beforePoints + 6, monitoredPlayer.getPoints());
        assertEquals(beforeFood + 1, monitoredPlayer.getFood());
    }

    // Test with 0 hunters and HuntEventBoostBC: no fixed bonus, only basic food +1.
    @Test
    void applyEvent_playerWithNoHuntersAndHuntEventBoostBuilding_getsNoPointsAndBaseFoodOnly() {
        Match match = new Match(java.util.List.of(new Player("p1"), new Player("p2")));
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);

        Player monitoredPlayer = match.getPlayers().get(0);
        assertTrue(monitoredPlayer.getHunters().isEmpty());

        monitoredPlayer.addBuilding(new HuntEventBoostBC(
                2001,
                Era.I,
                0,
                0,
                BuildingCardType.HuntEventBoostBC,
                false
        ));

        int beforePoints = monitoredPlayer.getPoints();
        int beforeFood = monitoredPlayer.getFood();

        huntEvent.applyEvent(match);

        assertEquals(beforePoints, monitoredPlayer.getPoints());
        assertEquals(beforeFood + 1, monitoredPlayer.getFood());
    }

    // Integration test: HuntEvent + HuntEventBoostBC with 3 hunters.
    // expected points = 3 * 2 + 3 = 9
    // expected food = 1 (base) + 3 (boost) = 4
    @Test
    void applyEvent_playerWithThreeHuntersAndHuntEventBoostBuilding_getsNinePointsAndFourFood() {
        Match match = new Match(java.util.List.of(new Player("p1"), new Player("p2")));
        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);

        Player monitoredPlayer = match.getPlayers().get(0);

        monitoredPlayer.addCharacter(new Hunter(3001, Era.I, CharacterType.HUNTER, false));
        monitoredPlayer.addCharacter(new Hunter(3002, Era.I, CharacterType.HUNTER, false));
        monitoredPlayer.addCharacter(new Hunter(3003, Era.I, CharacterType.HUNTER, false));
        monitoredPlayer.addBuilding(new HuntEventBoostBC(
                4001,
                Era.I,
                0,
                0,
                BuildingCardType.HuntEventBoostBC,
                false
        ));

        int beforePoints = monitoredPlayer.getPoints();
        int beforeFood = monitoredPlayer.getFood();

        huntEvent.applyEvent(match);

        assertEquals(beforePoints + 9, monitoredPlayer.getPoints());
        assertEquals(beforeFood + 4, monitoredPlayer.getFood());
    }

    // Test multiple players with different setups to verify isolated effects.
    // Player A: 0 hunters, no boost      → +1 food only
    // Player B: 2 hunters, no boost      → +1 food, +4 points (2*2)
    // Player C: 2 hunters, 1 boost       → +3 food (1+2), +6 points (2*2 + 2)
    @Test
    void applyEvent_multiplePlayersIndependentSetups_appliesEffectsCorrectly() {
        Player playerA = new Player("A");
        Player playerB = new Player("B");
        Player playerC = new Player("C");
        Match match = new Match(java.util.List.of(playerA, playerB, playerC));

        HuntEvent huntEvent = new HuntEvent(1, Era.I, false, EventEffect.HUNT_EVENT, 2);

        // Setup Player B: 2 hunters
        playerB.addCharacter(new Hunter(5001, Era.I, CharacterType.HUNTER, false));
        playerB.addCharacter(new Hunter(5002, Era.I, CharacterType.HUNTER, false));

        // Setup Player C: 2 hunters + 1 boost building
        playerC.addCharacter(new Hunter(5003, Era.I, CharacterType.HUNTER, false));
        playerC.addCharacter(new Hunter(5004, Era.I, CharacterType.HUNTER, false));
        playerC.addBuilding(new HuntEventBoostBC(
                5005,
                Era.I,
                0,
                0,
                BuildingCardType.HuntEventBoostBC,
                false
        ));

        // Store initial state
        int beforePointsA = playerA.getPoints();
        int beforeFoodA = playerA.getFood();
        int beforePointsB = playerB.getPoints();
        int beforeFoodB = playerB.getFood();
        int beforePointsC = playerC.getPoints();
        int beforeFoodC = playerC.getFood();

        // Apply event
        huntEvent.applyEvent(match);

        // Player A: 0 hunters, no boost → +1 food only
        assertEquals(beforePointsA, playerA.getPoints(), "Player A points should not change");
        assertEquals(beforeFoodA + 1, playerA.getFood(), "Player A should receive +1 base food");

        // Player B: 2 hunters, no boost → +1 food, +4 points (2*2)
        assertEquals(beforePointsB + 4, playerB.getPoints(), "Player B should receive +4 points (2*2)");
        assertEquals(beforeFoodB + 1, playerB.getFood(), "Player B should receive +1 base food");

        // Player C: 2 hunters, 1 boost → +3 food (1+2), +6 points (2*2 + 2)
        assertEquals(beforePointsC + 6, playerC.getPoints(), "Player C should receive +6 points (2*2 + 2 from boost)");
        assertEquals(beforeFoodC + 3, playerC.getFood(), "Player C should receive +3 food (1 base + 2 from boost)");
    }



}