package org.example.server;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.board.OfferTile;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.example.server.rmi.RMIClientCallback;
import org.example.server.rmi.RMIClientConnection;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerControllerTest {

    /**
     * Test double used to capture everything that the server sends to the client.
     * It can also be configured to fail on update or error delivery in order to test
     * the controller's unregister-on-failure behavior.
     */
    private static class RecordingCallback implements RMIClientCallback {
        private final List<GameStateUpdateMessage> updates = new ArrayList<>();
        private final List<LobbyUpdateMessage> lobbyUpdates = new ArrayList<>();
        private final List<RankingUpdateMessage> rankingUpdates = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        private final boolean failOnUpdate;
        private final boolean failOnError;

        private int shutdownCalls = 0;

        RecordingCallback() {
            this(false, false);
        }

        RecordingCallback(boolean failOnUpdate, boolean failOnError) {
            this.failOnUpdate = failOnUpdate;
            this.failOnError = failOnError;
        }

        @Override
        public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
            if (failOnUpdate) {
                throw new RemoteException("Simulated update failure");
            }
            updates.add(update);
        }

        @Override
        public void receiveError(String errorMessage) throws RemoteException {
            if (failOnError) {
                throw new RemoteException("Simulated error failure");
            }
            errors.add(errorMessage);
        }

        @Override
        public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
            lobbyUpdates.add(update);
        }

        @Override
        public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
            rankingUpdates.add(rankingUpdate);
        }

        @Override
        public void receiveShutdown() throws RemoteException {
            shutdownCalls++;
        }
    }

    /**
     * Match subclass used to force InvalidCardException from offerTileAction
     * without depending on a specific board setup.
     */
    private static class InvalidCardMatch extends Match {
        InvalidCardMatch(List<Player> players) {
            super(players);
        }

        @Override
        public void offerTileAction(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
            throw new InvalidCardException("Bad cards");
        }
    }

    /**
     * Match subclass used to force NoDrawableCardException from offerTileAction
     * in order to test the recovery path implemented by ServerController.
     */
    private static class NoDrawableCardMatch extends Match {
        NoDrawableCardMatch(List<Player> players) {
            super(players);
        }

        @Override
        public void offerTileAction(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
            throw new NoDrawableCardException("No drawable cards");
        }
    }

    /**
     * Utility factory that creates a minimal real match with two players.
     * Two players are enough to test turn validation and next-player transitions.
     */
    private Match createTwoPlayerMatch() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("alice"));
        players.add(new Player("bob"));
        return new Match(players);
    }

    /**
     * Utility factory used by the custom Match subclasses above.
     */
    private List<Player> createTwoPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("alice"));
        players.add(new Player("bob"));
        return players;
    }

    /**
     * Finds the non-current player in a two-player match.
     * This is useful for validating "wrong player" branches.
     */
    private Player findOtherPlayer(Match match) {
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        for (Player player : match.getPlayers()) {
            if (!player.getNickname().equals(currentNickname)) {
                return player;
            }
        }
        throw new IllegalStateException("Other player not found");
    }

    /**
     * Advances the match phase until PLAYER_TURN, which is required by skipTurn
     * and offerTileAction in the controller.
     */
    private void moveToPlayerTurn(Match match) {
        if (match.getGameState().getCurrentPhase() == GamePhase.PLACE_TOTEMS) {
            match.getGameState().advancePhase();
        }
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
    }

    @Test
    void registerClientAndUnregisterClient_shouldManageNicknameLookupCorrectly() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        RecordingCallback callback = new RecordingCallback();
        RMIClientConnection connection = new RMIClientConnection(callback);

        // Act
        controller.registerClient(connection, "alice");

        // Assert
        assertSame(connection, controller.getConnectionByNickname("alice"));

        // Act
        controller.unregisterClient(connection);

        // Assert
        assertThrows(IllegalArgumentException.class, () -> controller.getConnectionByNickname("alice"));
    }

    @Test
    void sendInitialState_shouldSendSnapshotToAllRegisteredClients() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        RecordingCallback firstCallback = new RecordingCallback();
        RecordingCallback secondCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(firstCallback), "alice");
        controller.registerClient(new RMIClientConnection(secondCallback), "bob");

        // Act
        assertDoesNotThrow(controller::sendInitialState);

        // Assert
        assertEquals(1, firstCallback.updates.size());
        assertEquals(1, secondCallback.updates.size());
    }

    @Test
    void placeTotemOnOfferTile_shouldSendErrorWhenPlayerIsNotCurrentPlayer() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        RecordingCallback currentCallback = new RecordingCallback();
        RecordingCallback otherCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(currentCallback), currentNickname);
        controller.registerClient(new RMIClientConnection(otherCallback), otherNickname);

        // Act
        controller.placeTotemOnOfferTile(otherNickname, 1);

        // Assert
        assertEquals(1, otherCallback.errors.size());
        assertEquals("Invalid move: it's not yourn turn or invalid phase.", otherCallback.errors.get(0));
        assertTrue(otherCallback.updates.isEmpty());
        assertTrue(currentCallback.updates.isEmpty());
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    @Test
    void placeTotemOnOfferTile_shouldAdvanceTurnAndNotifyClientsWhenMoveIsValid() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        RecordingCallback currentCallback = new RecordingCallback();
        RecordingCallback otherCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(currentCallback), currentNickname);
        controller.registerClient(new RMIClientConnection(otherCallback), otherNickname);

        // Act
        controller.placeTotemOnOfferTile(currentNickname, 1);

        // Assert
        OfferTile tile = match.getBoard().getOfferTrack().get(0);
        assertNotNull(tile.getPlayer());
        assertEquals(currentNickname, tile.getPlayer().getNickname());

        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());

        assertEquals(1, currentCallback.updates.size());
        assertEquals(1, otherCallback.updates.size());

        GameStateUpdateMessage updateForCurrent = currentCallback.updates.get(0);
        GameStateUpdateMessage updateForOther = otherCallback.updates.get(0);

        assertEquals(otherNickname, updateForCurrent.getCurrentPlayerNickname());
        assertEquals(otherNickname, updateForOther.getCurrentPlayerNickname());

        assertTrue(currentCallback.errors.isEmpty());
        assertTrue(otherCallback.errors.isEmpty());
    }

    @Test
    void skipTurn_shouldSendErrorWhenPhaseIsWrong() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        RecordingCallback callback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(callback), currentNickname);

        // Act
        controller.skipTurn(currentNickname);

        // Assert
        assertEquals(1, callback.errors.size());
        assertEquals("SkipTurn failed: invalid move: it's not yourn turn or invalid phase.", callback.errors.get(0));
        assertTrue(callback.updates.isEmpty());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    @Test
    void skipTurn_shouldAdvanceTurnAndNotifyClientsWhenPhaseIsPlayerTurn() {
        // Arrange
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);

        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        RecordingCallback currentCallback = new RecordingCallback();
        RecordingCallback otherCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(currentCallback), currentNickname);
        controller.registerClient(new RMIClientConnection(otherCallback), otherNickname);

        // Act
        controller.skipTurn(currentNickname);

        // Assert
        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());

        assertEquals(1, currentCallback.updates.size());
        assertEquals(1, otherCallback.updates.size());
        assertEquals(otherNickname, currentCallback.updates.get(0).getCurrentPlayerNickname());
        assertEquals(otherNickname, otherCallback.updates.get(0).getCurrentPlayerNickname());

        assertTrue(currentCallback.errors.isEmpty());
        assertTrue(otherCallback.errors.isEmpty());
    }

    @Test
    void offerTileAction_shouldSendErrorWhenMatchThrowsInvalidCardException() {
        // Arrange
        InvalidCardMatch match = new InvalidCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);

        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        RecordingCallback callback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(callback), currentNickname);

        // Act
        controller.offerTileAction(currentNickname, "1,2");

        // Assert
        assertEquals(1, callback.errors.size());
        assertEquals("Invalid move: Bad cards", callback.errors.get(0));
        assertTrue(callback.updates.isEmpty());
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
    }

    @Test
    void offerTileAction_shouldRecoverWhenMatchThrowsNoDrawableCardException() {
        // Arrange
        NoDrawableCardMatch match = new NoDrawableCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);

        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        RecordingCallback currentCallback = new RecordingCallback();
        RecordingCallback otherCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(currentCallback), currentNickname);
        controller.registerClient(new RMIClientConnection(otherCallback), otherNickname);

        // Manually place the current player on an offer tile so that the controller
        // can remove it in the NoDrawableCardException recovery branch.
        OfferTile selectedTile = match.getBoard().getOfferTrack().get(0);
        selectedTile.placePlayer(match.getGameState().getCurrentPlayer());

        // Act
        controller.offerTileAction(currentNickname, "");

        // Assert
        assertEquals(1, currentCallback.errors.size());
        assertEquals("No drawable cards", currentCallback.errors.get(0));

        assertNull(selectedTile.getPlayer());
        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());

        assertEquals(1, currentCallback.updates.size());
        assertEquals(1, otherCallback.updates.size());
        assertEquals(otherNickname, currentCallback.updates.get(0).getCurrentPlayerNickname());
        assertEquals(otherNickname, otherCallback.updates.get(0).getCurrentPlayerNickname());
    }

    @Test
    void sendInitialState_shouldUnregisterClientWhenSendingUpdateFails() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        RecordingCallback healthyCallback = new RecordingCallback();
        RecordingCallback failingCallback = new RecordingCallback(true, false);

        RMIClientConnection healthyConnection = new RMIClientConnection(healthyCallback);
        RMIClientConnection failingConnection = new RMIClientConnection(failingCallback);

        controller.registerClient(healthyConnection, "healthy");
        controller.registerClient(failingConnection, "failing");

        // Act + Assert
        // With the current implementation, notifyAll iterates directly on keySet()
        // and unregisters clients inside the loop. If your current ServerController
        // still has that implementation, this test may expose the bug.
        assertDoesNotThrow(controller::sendInitialState);

        assertEquals(1, healthyCallback.updates.size());
        assertThrows(IllegalArgumentException.class, () -> controller.getConnectionByNickname("failing"));
        assertSame(healthyConnection, controller.getConnectionByNickname("healthy"));
    }

    @Test
    void placeTotemOnOfferTile_shouldUnregisterClientWhenSendingErrorFails() {
        // Arrange
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String otherNickname = findOtherPlayer(match).getNickname();
        RecordingCallback failingOnErrorCallback = new RecordingCallback(false, true);
        RMIClientConnection failingConnection = new RMIClientConnection(failingOnErrorCallback);

        controller.registerClient(failingConnection, otherNickname);

        // Act
        controller.placeTotemOnOfferTile(otherNickname, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, () -> controller.getConnectionByNickname(otherNickname));
    }

    @Test
    void placeTotemOnOfferTile_shouldReturnWhenConnectionNotFound() {
        // Arrange: create a controller without registering any client
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act + Assert: the method should return quietly if no client connection is found
        assertDoesNotThrow(() -> controller.placeTotemOnOfferTile(currentNickname, 1));

        // The game state must remain unchanged
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
    }

    @Test
    void offerTileAction_shouldReturnWhenConnectionNotFound() {
        // Arrange: move the match to PLAYER_TURN but do not register the client
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act + Assert: the controller should return without throwing
        assertDoesNotThrow(() -> controller.offerTileAction(currentNickname, ""));

        // The game state must remain unchanged
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
    }

    @Test
    void skipTurn_shouldReturnWhenConnectionNotFound() {
        // Arrange: move the match to PLAYER_TURN but do not register the client
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act + Assert: the controller should return without throwing
        assertDoesNotThrow(() -> controller.skipTurn(currentNickname));

        // The game state must remain unchanged
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
    }

    @Test
    void offerTileAction_shouldSendErrorWhenWrongPlayer() {
        // Arrange: move the match to PLAYER_TURN and register both clients
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        RecordingCallback currentCallback = new RecordingCallback();
        RecordingCallback otherCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(currentCallback), currentNickname);
        controller.registerClient(new RMIClientConnection(otherCallback), otherNickname);

        // Act: the non-current player tries to perform an action
        controller.offerTileAction(otherNickname, "");

        // Assert: only the wrong player should receive the validation error
        assertEquals(1, otherCallback.errors.size());
        assertEquals("Invalid move: it's not yourn turn or invalid phase.", otherCallback.errors.get(0));
        assertTrue(currentCallback.updates.isEmpty());
        assertTrue(otherCallback.updates.isEmpty());
    }

    @Test
    void offerTileAction_shouldSendErrorWhenWrongPhase() {
        // Arrange: stay in PLACE_TOTEMS and register the current player
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        RecordingCallback callback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(callback), currentNickname);

        // Act: the player tries to perform an offer tile action in the wrong phase
        controller.offerTileAction(currentNickname, "");

        // Assert: the controller must reject the move with an error
        assertEquals(1, callback.errors.size());
        assertEquals("Invalid move: it's not yourn turn or invalid phase.", callback.errors.get(0));
        assertTrue(callback.updates.isEmpty());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    /**
     * Match subclass used to force a generic runtime exception from offerTileAction.
     * This allows us to cover the generic catch branch in ServerController.
     */
    private static class GenericFailureMatch extends Match {
        GenericFailureMatch(List<Player> players) {
            super(players);
        }

        @Override
        public void offerTileAction(Player player, String cards) {
            throw new RuntimeException("Boom");
        }
    }

    @Test
    void offerTileAction_shouldSendErrorWhenGenericExceptionOccurs() {
        // Arrange: move the custom match to PLAYER_TURN
        GenericFailureMatch match = new GenericFailureMatch(createTwoPlayers());
        moveToPlayerTurn(match);

        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        RecordingCallback callback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(callback), currentNickname);

        // Act: the model throws a generic runtime exception
        controller.offerTileAction(currentNickname, "1");

        // Assert: the controller must wrap it into the generic error message
        assertEquals(1, callback.errors.size());
        assertEquals("Generic Exception: Boom", callback.errors.get(0));
        assertTrue(callback.updates.isEmpty());
    }

    /**
     * Match subclass used to force an exception during placeTotemOnOfferTile.
     * This allows us to cover the catch block in the controller method.
     */
    private static class FailingPlaceTotemMatch extends Match {
        FailingPlaceTotemMatch(List<Player> players) {
            super(players);
        }

        @Override
        public void placeTotemOnOfferTile(Player player, int tile) {
            throw new IllegalArgumentException("Invalid tile");
        }
    }

    @Test
    void placeTotemOnOfferTile_shouldSendErrorWhenModelThrowsException() {
        // Arrange: create a match whose placeTotemOnOfferTile always fails
        FailingPlaceTotemMatch match = new FailingPlaceTotemMatch(createTwoPlayers());
        ServerController controller = new ServerController(match);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        RecordingCallback callback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(callback), currentNickname);

        // Act: trigger the controller branch that catches model exceptions
        controller.placeTotemOnOfferTile(currentNickname, 99);

        // Assert: the controller must send the formatted error to the sender
        assertEquals(1, callback.errors.size());
        assertEquals("Invalid move: Invalid tile", callback.errors.get(0));
        assertTrue(callback.updates.isEmpty());
    }

    @Test
    void placeTotemOnOfferTile_lastPlayerShouldSwitchPhaseToPlayerTurnAndNotifyOrderFromOfferTrack() {
        // Arrange: register both players and let both place their totems
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match);

        String firstNickname = match.getGameState().getCurrentPlayer().getNickname();
        String secondNickname = findOtherPlayer(match).getNickname();

        RecordingCallback firstCallback = new RecordingCallback();
        RecordingCallback secondCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(firstCallback), firstNickname);
        controller.registerClient(new RMIClientConnection(secondCallback), secondNickname);

        // Act: first player places on tile 1, second player places on tile 2
        controller.placeTotemOnOfferTile(firstNickname, 1);
        controller.placeTotemOnOfferTile(secondNickname, 2);

        // Assert: after the last player acts, the phase must switch to PLAYER_TURN
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());

        // The offer track order should become the new turn order
        assertEquals(firstNickname, match.getGameState().getCurrentPlayer().getNickname());

        GameStateUpdateMessage lastUpdate = firstCallback.updates.get(firstCallback.updates.size() - 1);
        assertEquals(GamePhase.PLAYER_TURN, lastUpdate.getCurrentPhase());
        assertEquals(firstNickname, lastUpdate.getCurrentPlayerNickname());
        assertEquals(List.of(firstNickname, secondNickname), lastUpdate.getTurnOrder());
    }

    @Test
    void skipTurn_lastPlayerShouldAdvanceThroughEndRoundToNextRound() {
        // Arrange: start directly from PLAYER_TURN and register both players
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);

        ServerController controller = new ServerController(match);

        String firstNickname = match.getGameState().getCurrentPlayer().getNickname();
        String secondNickname = findOtherPlayer(match).getNickname();

        RecordingCallback firstCallback = new RecordingCallback();
        RecordingCallback secondCallback = new RecordingCallback();

        controller.registerClient(new RMIClientConnection(firstCallback), firstNickname);
        controller.registerClient(new RMIClientConnection(secondCallback), secondNickname);

        // Act: both players skip, so the second skip is performed by the last player
        controller.skipTurn(firstNickname);
        controller.skipTurn(secondNickname);

        // Assert: the controller must automatically resolve phase transitions
        // and reach the next round in PLACE_TOTEMS
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
        assertEquals(2, match.getGameState().getCurrentRound());

        GameStateUpdateMessage lastUpdate = firstCallback.updates.get(firstCallback.updates.size() - 1);
        assertEquals(GamePhase.PLACE_TOTEMS, lastUpdate.getCurrentPhase());
        assertEquals(2, lastUpdate.getCurrentRound());
    }

}