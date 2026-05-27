package org.example.server;

import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;
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

}