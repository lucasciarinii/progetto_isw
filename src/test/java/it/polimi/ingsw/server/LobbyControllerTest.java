package it.polimi.ingsw.server;

import it.polimi.ingsw.network.ServerNotifier;
import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.server.model.enums.GamePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LobbyControllerTest {

    /**
     * Fake listener to record when the lobby becomes full and ready.
     */
    private static class FakeLobbyReadyListener implements LobbyReadyListener {
        int readyCalls = 0;
        ServerController lastCreatedController = null;
        String lastGameId = null;

        @Override
        public void onLobbyReady(ServerController serverController, String gameID) {
            readyCalls++;
            lastCreatedController = serverController;
            lastGameId = gameID;
        }
    }

    /**
     * Fake notifier to intercept all messages sent by the controller to the network.
     */
    private static class FakeServerNotifier implements ServerNotifier {
        // Maps to store messages sent to each specific player nickname
        final Map<String, List<LobbyUpdateMessage>> lobbyUpdates = new HashMap<>();
        final Map<String, List<String>> errors = new HashMap<>();

        // Flag to simulate a network crash for a specific player
        boolean simulateFailureForAlice = false;

        @Override
        public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
            if (simulateFailureForAlice && "alice".equals(nickname)) {
                throw new Exception("Simulated network failure for Alice");
            }
            lobbyUpdates.computeIfAbsent(nickname, k -> new ArrayList<>()).add(update);
        }

        @Override
        public void sendError(String nickname, String message, GamePhase phase) throws Exception {
            errors.computeIfAbsent(nickname, k -> new ArrayList<>()).add(message);
        }

        // --- Other ServerNotifier methods (stubbed as empty for this test) ---
        @Override public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {}
        @Override public void sendRoundFlowCardRequest(String nickname) throws Exception {}
        @Override public void sendRankingUpdate(String nickname, RankingUpdateMessage msg) throws Exception {}
        @Override public void sendShutdown(String nickname) throws Exception {}
        @Override public void handleClientDisconnect(String nickname, String reason) {}
    }

    private FakeLobbyReadyListener fakeListener;
    private FakeServerNotifier fakeNotifier;
    private LobbyController lobbyController;

    @BeforeEach
    void setUp() {
        fakeListener = new FakeLobbyReadyListener();
        fakeNotifier = new FakeServerNotifier();
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "TEST01", 3);
    }

    @Test
    void registerPlayer_ShouldRejectDuplicateNickname() throws Exception {
        // Arrange: Register the first player successfully
        lobbyController.registerPlayer("alice");

        // Act & Assert: Attempt to register another client with the exact same nickname
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> lobbyController.registerPlayer("alice")
        );
        assertTrue(exception.getMessage().contains("Nickname already used"));
    }

    @Test
    void registerPlayer_ShouldNotifyWaitingClientsAfterRegistration() throws Exception {
        // Act: Register the first player with a valid lobby size
        lobbyController.registerPlayer("alice");

        // Assert: Alice should receive exactly one lobby update
        List<LobbyUpdateMessage> aliceUpdates = fakeNotifier.lobbyUpdates.get("alice");
        assertNotNull(aliceUpdates);
        assertEquals(1, aliceUpdates.size());

        LobbyUpdateMessage update = aliceUpdates.get(0);
        assertEquals(1, update.getConnectedPlayers());
        assertEquals(3, update.getRequiredPlayers());
        assertTrue(update.getPlayerNicknames().contains("alice"));
        assertFalse(update.isGameStarting());
    }

    @Test
    void registerPlayer_ShouldPreserveConnectionOrder() throws Exception {
        // Act: Register two players in sequence
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob");

        // Assert: The latest update sent to Bob should preserve insertion order
        List<LobbyUpdateMessage> bobUpdates = fakeNotifier.lobbyUpdates.get("bob");
        LobbyUpdateMessage lastUpdate = bobUpdates.get(bobUpdates.size() - 1);

        assertEquals("alice", lastUpdate.getPlayerNicknames().get(0));
        assertEquals("bob", lastUpdate.getPlayerNicknames().get(1));
    }

    @Test
    void registerPlayer_ShouldStartGameWhenLobbyBecomesFull() throws Exception {
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "START2", 2);

        // Act: Fill a 2-player lobby
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob");

        // Assert: The lobby must trigger the ready listener and create a ServerController
        assertEquals(1, fakeListener.readyCalls);
        assertNotNull(fakeListener.lastCreatedController);

        // Retrieve the lists of updates sent to Alice and Bob
        List<LobbyUpdateMessage> aliceUpdates = fakeNotifier.lobbyUpdates.get("alice");
        List<LobbyUpdateMessage> bobUpdates = fakeNotifier.lobbyUpdates.get("bob");

        // Get the very LAST update message sent to each player
        LobbyUpdateMessage aliceLastUpdate = aliceUpdates.get(aliceUpdates.size() - 1);
        LobbyUpdateMessage bobLastUpdate = bobUpdates.get(bobUpdates.size() - 1);

        // Check that the last message has the "gameStarting = true" flag
        assertTrue(aliceLastUpdate.isGameStarting());
        assertTrue(bobLastUpdate.isGameStarting());
    }

    @Test
    void notifyAllWaiting_ShouldIgnoreNetworkFailuresAndContinue() throws Exception {
        // Arrange: Simulate a network crash specifically for Alice
        fakeNotifier.simulateFailureForAlice = true;

        // Act: Register Alice (which will fail silently during notification) and then Bob
        lobbyController.registerPlayer("alice");
        assertDoesNotThrow(() -> lobbyController.registerPlayer("bob"));

        // Assert: Bob must still receive his update successfully despite Alice's failure
        assertNotNull(fakeNotifier.lobbyUpdates.get("bob"));
        assertFalse(fakeNotifier.lobbyUpdates.get("bob").isEmpty());
    }

    /**
     * Verifies that registering a player in a full lobby throws an IllegalStateException.
     */
    @Test
    void registerPlayer_ShouldThrowWhenLobbyAlreadyFull() {
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "FULL01", 2);
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob"); // lobby full → game started

        assertThrows(IllegalStateException.class,
                () -> lobbyController.registerPlayer("charlie"));
    }

    /**
     * Verifies that registering a player after the game has already started
     * throws an IllegalStateException with the appropriate message.
     */
    @Test
    void registerPlayer_ShouldThrowWhenLobbyAlreadyStarted() {
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "STARTED01", 2);
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob"); // triggers start → started = true

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> lobbyController.registerPlayer("charlie"));
        assertTrue(ex.getMessage().contains("already started"));
    }

    /**
     * Verifies that isNicknameTaken returns true for a registered player
     * and false for a nickname not yet in the lobby.
     */
    @Test
    void isNicknameTaken_ShouldReturnTrueForRegisteredPlayerAndFalseOtherwise() {
        lobbyController.registerPlayer("alice");

        assertTrue(lobbyController.isNicknameTaken("alice"));
        assertFalse(lobbyController.isNicknameTaken("bob"));
    }

    /**
     * Verifies that isStarted returns false before the lobby fills up
     * and true immediately after the last required player joins.
     */
    @Test
    void isStarted_ShouldReturnFalseBeforeGameStartsAndTrueAfter() {
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "STATE01", 2);

        assertFalse(lobbyController.isStarted());

        lobbyController.registerPlayer("alice");
        assertFalse(lobbyController.isStarted());

        lobbyController.registerPlayer("bob");
        assertTrue(lobbyController.isStarted());
    }

    /**
     * Verifies that getWaitingNicknames returns a snapshot of the current players
     * and that modifying the returned list does not affect the internal state.
     */
    @Test
    void getWaitingNicknames_ShouldReturnSnapshotOfCurrentPlayers() {
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob");

        List<String> nicknames = lobbyController.getWaitingNicknames();

        assertEquals(2, nicknames.size());
        assertTrue(nicknames.contains("alice"));
        assertTrue(nicknames.contains("bob"));

        // Verify it is a defensive copy
        nicknames.add("hacker");
        assertEquals(2, lobbyController.getWaitingNicknames().size());
    }

    /**
     * Verifies that cancelLobby sends an error message and a shutdown event
     * to all players currently waiting in the lobby.
     */
    @Test
    void cancelLobby_ShouldSendErrorAndShutdownToAllWaitingPlayers() {
        List<String> shutdownReceived = new ArrayList<>();
        ServerNotifier trackingNotifier = new FakeServerNotifier() {
            @Override
            public void sendShutdown(String nickname) {
                shutdownReceived.add(nickname);
            }
        };

        LobbyController lobby = new LobbyController(fakeListener, trackingNotifier, "CANCEL01", 3);
        lobby.registerPlayer("alice");
        lobby.registerPlayer("bob");

        lobby.cancelLobby("Server shutting down");

        assertTrue(shutdownReceived.contains("alice"));
        assertTrue(shutdownReceived.contains("bob"));

        FakeServerNotifier fn = (FakeServerNotifier) trackingNotifier;
        assertTrue(fn.errors.containsKey("alice"));
        assertTrue(fn.errors.containsKey("bob"));
        assertTrue(fn.errors.get("alice").get(0).contains("Server shutting down"));
    }

    /**
     * Verifies that cancelLobby does not propagate exceptions
     * when a notification fails due to a network error.
     */
    @Test
    void cancelLobby_ShouldNotThrowWhenNotificationFails() {
        ServerNotifier failingNotifier = new FakeServerNotifier() {
            @Override
            public void sendError(String nickname, String message, GamePhase phase) throws Exception {
                throw new Exception("Network down");
            }
        };

        LobbyController lobby = new LobbyController(fakeListener, failingNotifier, "CANCEL02", 2);
        lobby.registerPlayer("alice");

        assertDoesNotThrow(() -> lobby.cancelLobby("Forced shutdown"));
    }

    /**
     * Verifies that all players already in the lobby receive a new update message
     * each time a new player joins, and that the update reflects the correct state.
     */
    @Test
    void registerPlayer_ShouldNotifyAllExistingPlayersWhenNewOneJoins() {
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob");

        // Alice should receive 2 updates: one when she joined, one when bob joined
        List<LobbyUpdateMessage> aliceUpdates = fakeNotifier.lobbyUpdates.get("alice");
        assertEquals(2, aliceUpdates.size());

        LobbyUpdateMessage secondUpdate = aliceUpdates.get(1);
        assertEquals(2, secondUpdate.getConnectedPlayers());
        assertTrue(secondUpdate.getPlayerNicknames().contains("bob"));
    }

    /**
     * Verifies that onLobbyReady is called exactly once with the correct game ID
     * when the lobby reaches the required number of players.
     */
    @Test
    void startGame_ShouldCallOnLobbyReadyWithCorrectGameID() {
        lobbyController = new LobbyController(fakeListener, fakeNotifier, "GAMEID42", 2);
        lobbyController.registerPlayer("alice");
        lobbyController.registerPlayer("bob");

        assertEquals("GAMEID42", fakeListener.lastGameId);
        assertEquals(1, fakeListener.readyCalls);
    }

}