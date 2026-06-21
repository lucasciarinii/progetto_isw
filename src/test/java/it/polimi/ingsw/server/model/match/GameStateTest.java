package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.GamePhase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    //Test that the constructor initializes the default game state correctly.
    @Test
    void constructor_shouldInitializeDefaultStateCorrectly() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");

        GameState gameState = new GameState(List.of(p1, p2));

        assertEquals(1, gameState.getCurrentRound());
        assertEquals(Era.I, gameState.getCurrentEra());
        assertEquals(GamePhase.PLACE_TOTEMS, gameState.getCurrentPhase());
        assertEquals(List.of(p1, p2), gameState.getTurnOrder());
        assertSame(p1, gameState.getCurrentPlayer());
        assertFalse(gameState.isGameOver());
        assertTrue(gameState.getWinners().isEmpty());
    }

    //Test that the constructor rejects a null turn order.
    @Test
    void constructor_shouldThrowException_whenTurnOrderIsNull() {
        assertThrows(NullPointerException.class, () -> new GameState(null));
    }

    //Test that advancing the round increases the current round by one.
    @Test
    void advanceRound_shouldIncreaseCurrentRoundByOne() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advanceRound();

        assertEquals(2, gameState.getCurrentRound());
    }

    //Test that advancing the era moves from Era I to Era II.
    @Test
    void advanceCurrentEra_shouldMoveFromEraIToEraII() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advanceCurrentEra();

        assertEquals(Era.II, gameState.getCurrentEra());
    }

    //Test that advancing the era twice moves from Era I to Era III.
    @Test
    void advanceCurrentEra_shouldMoveFromEraIIToEraIII() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advanceCurrentEra();
        gameState.advanceCurrentEra();

        assertEquals(Era.III, gameState.getCurrentEra());
    }

    //Test that advancing the era beyond Era III throws an exception.
    @Test
    void advanceCurrentEra_shouldThrowException_whenAlreadyInEraIII() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advanceCurrentEra();
        gameState.advanceCurrentEra();

        assertThrows(IllegalStateException.class, gameState::advanceCurrentEra);
    }

    //Test that advancing the phase moves from PLACE_TOTEMS to PLAYER_TURN.
    @Test
    void advancePhase_shouldMoveFromPlaceTotemsToPlayerTurn() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advancePhase();

        assertEquals(GamePhase.PLAYER_TURN, gameState.getCurrentPhase());
    }

    //Test that advancing the phase twice moves from PLACE_TOTEMS to EVENTS_RESOLVE.
    @Test
    void advancePhase_shouldMoveFromPlayerTurnToEventsResolve() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advancePhase();
        gameState.advancePhase();

        assertEquals(GamePhase.EVENTS_RESOLVE, gameState.getCurrentPhase());
    }

    //Test that advancing the phase three times moves from PLACE_TOTEMS to END_ROUND.
    @Test
    void advancePhase_shouldMoveFromEventsResolveToEndRound() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();

        assertEquals(GamePhase.END_ROUND, gameState.getCurrentPhase());
    }

    //Test that END_ROUND before round ten advances the round and returns to PLACE_TOTEMS.
    @Test
    void advancePhase_fromEndRoundBeforeRoundTen_shouldAdvanceRoundAndReturnToPlaceTotems() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();

        assertEquals(2, gameState.getCurrentRound());
        assertEquals(GamePhase.PLACE_TOTEMS, gameState.getCurrentPhase());
    }

    //Test that END_ROUND at round ten moves to END_GAME without increasing the round.
    @Test
    void advancePhase_fromEndRoundAtRoundTen_shouldMoveToEndGameWithoutIncreasingRound() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        for (int i = 1; i < 10; i++) {
            gameState.advanceRound();
        }

        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();

        assertEquals(10, gameState.getCurrentRound());
        assertEquals(GamePhase.END_GAME, gameState.getCurrentPhase());
    }

    //Test that advancing the phase from END_GAME moves to GAME_OVER.
    @Test
    void advancePhase_fromEndGame_shouldMoveToGameOver() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        for (int i = 1; i < 10; i++) {
            gameState.advanceRound();
        }

        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();
        gameState.advancePhase();

        assertEquals(GamePhase.GAME_OVER, gameState.getCurrentPhase());
        assertTrue(gameState.isGameOver());
    }

    //Test that advancing the phase from GAME_OVER keeps the phase unchanged.
    @Test
    void advancePhase_fromGameOver_shouldRemainGameOver() {
        GameState gameState = new GameState(List.of(new Player("Alice")));

        gameState.setWinners(List.of(new Player("Alice")));

        gameState.advancePhase();

        assertEquals(GamePhase.GAME_OVER, gameState.getCurrentPhase());
        assertTrue(gameState.isGameOver());
    }

    //Test that the turn order returned by the getter is unmodifiable.
    @Test
    void getTurnOrder_shouldReturnUnmodifiableList() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        GameState gameState = new GameState(List.of(p1, p2));

        assertThrows(UnsupportedOperationException.class,
                () -> gameState.getTurnOrder().add(new Player("Charlie")));
    }

    //Test that updating the turn order replaces the order and resets the current player index.
    @Test
    void updateTurnOrder_shouldReplaceOrderAndResetCurrentPlayerIndex() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        Player p3 = new Player("Charlie");

        GameState gameState = new GameState(List.of(p1, p2));
        gameState.advanceToNextPlayer();

        gameState.updateTurnOrder(List.of(p3, p1, p2));

        assertEquals(List.of(p3, p1, p2), gameState.getTurnOrder());
        assertSame(p3, gameState.getCurrentPlayer());
    }

    //Test that updateTurnOrder makes a defensive copy of the provided list.
    @Test
    void updateTurnOrder_shouldCreateDefensiveCopyOfInputList() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        ArrayList<Player> newOrder = new ArrayList<>(List.of(p1, p2));

        GameState gameState = new GameState(List.of(p1));
        gameState.updateTurnOrder(newOrder);

        newOrder.add(new Player("Charlie"));

        assertEquals(2, gameState.getTurnOrder().size());
        assertEquals(List.of(p1, p2), gameState.getTurnOrder());
    }

    //Test that getCurrentPlayer returns the player at the current index.
    @Test
    void getCurrentPlayer_shouldReturnPlayerAtCurrentIndex() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");

        GameState gameState = new GameState(List.of(p1, p2));

        assertSame(p1, gameState.getCurrentPlayer());

        gameState.advanceToNextPlayer();

        assertSame(p2, gameState.getCurrentPlayer());
    }

    //Test that advancing to the next player moves the index forward when the current player is not the last one.
    @Test
    void advanceToNextPlayer_shouldMoveToNextPlayer_whenCurrentPlayerIsNotLast() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        Player p3 = new Player("Charlie");

        GameState gameState = new GameState(List.of(p1, p2, p3));

        gameState.advanceToNextPlayer();

        assertSame(p2, gameState.getCurrentPlayer());
        assertEquals(GamePhase.PLACE_TOTEMS, gameState.getCurrentPhase());
    }

    //Test that advancing from the last player resets the index and advances the phase.
    @Test
    void advanceToNextPlayer_shouldResetIndexAndAdvancePhase_whenCurrentPlayerIsLast() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");

        GameState gameState = new GameState(List.of(p1, p2));

        gameState.advanceToNextPlayer();
        gameState.advanceToNextPlayer();

        assertSame(p1, gameState.getCurrentPlayer());
        assertEquals(GamePhase.PLAYER_TURN, gameState.getCurrentPhase());
    }

    //Test that setting winners stores the winners and switches the phase to GAME_OVER.
    @Test
    void setWinners_shouldStoreWinnersAndSetGameOverPhase() {
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");

        GameState gameState = new GameState(List.of(p1, p2));
        gameState.setWinners(List.of(p1, p2));

        assertEquals(List.of(p1, p2), gameState.getWinners());
        assertEquals(GamePhase.GAME_OVER, gameState.getCurrentPhase());
        assertTrue(gameState.isGameOver());
    }

    //Test that the winners list returned by the getter is unmodifiable.
    @Test
    void getWinners_shouldReturnUnmodifiableList() {
        Player p1 = new Player("Alice");

        GameState gameState = new GameState(List.of(p1));
        gameState.setWinners(List.of(p1));

        assertThrows(UnsupportedOperationException.class,
                () -> gameState.getWinners().add(new Player("Bob")));
    }
}