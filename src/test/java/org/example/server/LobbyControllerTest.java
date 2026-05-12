//package org.example.server;
//
//import org.example.network.GameStateUpdateMessage;
//import org.example.network.LobbyUpdateMessage;
//import org.example.network.RankingUpdateMessage;
//import org.example.server.rmi.RMIClientCallback;
//import org.junit.jupiter.api.Test;
//
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class LobbyControllerTest {
//
//    /**
//     * Test double used to record every message sent by the lobby or the server.
//     * It can also simulate a failure when receiving lobby updates.
//     */
//    private static class RecordingCallback implements RMIClientCallback {
//        private final List<String> errors = new ArrayList<>();
//        private final List<LobbyUpdateMessage> lobbyUpdates = new ArrayList<>();
//        private final List<GameStateUpdateMessage> gameUpdates = new ArrayList<>();
//        private final List<RankingUpdateMessage> rankingUpdates = new ArrayList<>();
//
//        private final boolean failOnLobbyUpdate;
//        private int shutdownCalls = 0;
//
//        RecordingCallback() {
//            this(false);
//        }
//
//        RecordingCallback(boolean failOnLobbyUpdate) {
//            this.failOnLobbyUpdate = failOnLobbyUpdate;
//        }
//
//        @Override
//        public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
//            gameUpdates.add(update);
//        }
//
//        @Override
//        public void receiveError(String errorMessage) throws RemoteException {
//            errors.add(errorMessage);
//        }
//
//        @Override
//        public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
//            if (failOnLobbyUpdate) {
//                throw new RemoteException("Simulated lobby update failure");
//            }
//            lobbyUpdates.add(update);
//        }
//
//        @Override
//        public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
//            rankingUpdates.add(rankingUpdate);
//        }
//
//        @Override
//        public void receiveShutdown() throws RemoteException {
//            shutdownCalls++;
//        }
//    }
//
//    /**
//     * Test double used to record when the lobby becomes ready
//     * and to capture the created ServerController.
//     */
//    private static class RecordingLobbyReadyListener implements LobbyReadyListener {
//        private int readyCalls = 0;
//        private ServerController lastController = null;
//
//        @Override
//        public void onLobbyReady(ServerController serverController) {
//            readyCalls++;
//            lastController = serverController;
//        }
//    }
//
//    @Test
//    void registerPlayer_shouldRejectInvalidNumberOfPlayersForFirstPlayer() throws Exception {
//        // Arrange: create a fresh lobby and a callback for the first player
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//        RecordingCallback callback = new RecordingCallback();
//
//        // Act: the first player chooses an invalid number of players
//        lobbyController.registerPlayer("alice", 1, callback);
//
//        // Assert: the player must receive an error and no lobby update
//        assertEquals(1, callback.errors.size());
//        assertEquals("Invalid number of players. Choose between 2 and 5.", callback.errors.get(0));
//        assertTrue(callback.lobbyUpdates.isEmpty());
//        assertEquals(0, readyListener.readyCalls);
//    }
//
//    @Test
//    void registerPlayer_shouldRejectDuplicateNickname() throws Exception {
//        // Arrange: register the first player successfully
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstCallback = new RecordingCallback();
//        RecordingCallback duplicateCallback = new RecordingCallback();
//
//        lobbyController.registerPlayer("alice", 2, firstCallback);
//
//        // Act: try to register another client with the same nickname
//        lobbyController.registerPlayer("alice", 2, duplicateCallback);
//
//        // Assert: the duplicate client must receive an error and no lobby update
//        assertEquals(1, duplicateCallback.errors.size());
//        assertEquals("Nickname already used: alice", duplicateCallback.errors.get(0));
//        assertTrue(duplicateCallback.lobbyUpdates.isEmpty());
//
//        // The original client must only have the update from the first successful registration
//        assertEquals(1, firstCallback.lobbyUpdates.size());
//        assertEquals(0, readyListener.readyCalls);
//    }
//
//    @Test
//    void registerPlayer_shouldNotifyWaitingClientsAfterFirstRegistration() throws Exception {
//        // Arrange: create a fresh lobby
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//        RecordingCallback firstCallback = new RecordingCallback();
//
//        // Act: register the first player with a valid lobby size
//        lobbyController.registerPlayer("alice", 3, firstCallback);
//
//        // Assert: the first player must receive a waiting-lobby update
//        assertEquals(1, firstCallback.lobbyUpdates.size());
//
//        LobbyUpdateMessage update = firstCallback.lobbyUpdates.get(0);
//        assertEquals(1, update.getConnectedPlayers());
//        assertEquals(3, update.getRequiredPlayers());
//        assertEquals(List.of("alice"), update.getPlayerNicknames());
//        assertFalse(update.isGameStarting());
//
//        assertTrue(firstCallback.errors.isEmpty());
//        assertEquals(0, readyListener.readyCalls);
//    }
//
//    @Test
//    void registerPlayer_shouldPreserveConnectionOrderInLobbyUpdates() throws Exception {
//        // Arrange: create a lobby and two healthy callbacks
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstCallback = new RecordingCallback();
//        RecordingCallback secondCallback = new RecordingCallback();
//
//        // Act: register two players in sequence
//        lobbyController.registerPlayer("alice", 3, firstCallback);
//        lobbyController.registerPlayer("bob", 3, secondCallback);
//
//        // Assert: the latest waiting update must preserve insertion order
//        LobbyUpdateMessage updateSeenByFirst = firstCallback.lobbyUpdates.get(firstCallback.lobbyUpdates.size() - 1);
//        LobbyUpdateMessage updateSeenBySecond = secondCallback.lobbyUpdates.get(secondCallback.lobbyUpdates.size() - 1);
//
//        assertEquals(List.of("alice", "bob"), updateSeenByFirst.getPlayerNicknames());
//        assertEquals(List.of("alice", "bob"), updateSeenBySecond.getPlayerNicknames());
//        assertEquals(2, updateSeenByFirst.getConnectedPlayers());
//        assertEquals(3, updateSeenByFirst.getRequiredPlayers());
//        assertFalse(updateSeenByFirst.isGameStarting());
//    }
//
//    @Test
//    void registerPlayer_shouldStartGameWhenLobbyBecomesFull() throws Exception {
//        // Arrange: create a 2-player lobby
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstCallback = new RecordingCallback();
//        RecordingCallback secondCallback = new RecordingCallback();
//
//        // Act: fill the lobby
//        lobbyController.registerPlayer("alice", 2, firstCallback);
//        lobbyController.registerPlayer("bob", 2, secondCallback);
//
//        // Assert: the lobby must start the game and notify the ready listener
//        assertEquals(1, readyListener.readyCalls);
//        assertNotNull(readyListener.lastController);
//
//        // Both clients must have received a "game starting" lobby update
//        LobbyUpdateMessage firstLastLobbyUpdate = firstCallback.lobbyUpdates.get(firstCallback.lobbyUpdates.size() - 1);
//        LobbyUpdateMessage secondLastLobbyUpdate = secondCallback.lobbyUpdates.get(secondCallback.lobbyUpdates.size() - 1);
//
//        assertTrue(firstLastLobbyUpdate.isGameStarting());
//        assertTrue(secondLastLobbyUpdate.isGameStarting());
//        assertEquals(2, firstLastLobbyUpdate.getConnectedPlayers());
//        assertEquals(2, firstLastLobbyUpdate.getRequiredPlayers());
//        assertEquals(List.of("alice", "bob"), firstLastLobbyUpdate.getPlayerNicknames());
//
//        // After startGame, each client must also receive the initial game snapshot
//        assertEquals(1, firstCallback.gameUpdates.size());
//        assertEquals(1, secondCallback.gameUpdates.size());
//    }
//
//    @Test
//    void registerPlayer_shouldNotifyAllWaitingClientsWhenANewPlayerJoins() throws Exception {
//        // Arrange: create a 3-player lobby
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstCallback = new RecordingCallback();
//        RecordingCallback secondCallback = new RecordingCallback();
//
//        // Act: register two players
//        lobbyController.registerPlayer("alice", 3, firstCallback);
//        lobbyController.registerPlayer("bob", 3, secondCallback);
//
//        // Assert:
//        // - the first player receives one update after self-registration
//        //   and another after the second player joins
//        // - the second player receives one update when joining
//        assertEquals(2, firstCallback.lobbyUpdates.size());
//        assertEquals(1, secondCallback.lobbyUpdates.size());
//
//        LobbyUpdateMessage firstLatest = firstCallback.lobbyUpdates.get(firstCallback.lobbyUpdates.size() - 1);
//        LobbyUpdateMessage secondLatest = secondCallback.lobbyUpdates.get(secondCallback.lobbyUpdates.size() - 1);
//
//        assertEquals(2, firstLatest.getConnectedPlayers());
//        assertEquals(2, secondLatest.getConnectedPlayers());
//        assertEquals(List.of("alice", "bob"), firstLatest.getPlayerNicknames());
//        assertEquals(List.of("alice", "bob"), secondLatest.getPlayerNicknames());
//        assertFalse(firstLatest.isGameStarting());
//        assertFalse(secondLatest.isGameStarting());
//    }
//
//    @Test
//    void notifyAllWaiting_shouldIgnoreCallbackFailuresAndContinueNotifyingOthers() throws Exception {
//        // Arrange: first client fails on lobby update, second client is healthy
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback failingCallback = new RecordingCallback(true);
//        RecordingCallback healthyCallback = new RecordingCallback();
//
//        // Act: register both players in a 3-player lobby
//        lobbyController.registerPlayer("alice", 3, failingCallback);
//        lobbyController.registerPlayer("bob", 3, healthyCallback);
//
//        // Assert:
//        // the failure of one callback must not prevent the healthy client
//        // from receiving the lobby update
//        assertTrue(healthyCallback.lobbyUpdates.size() >= 1);
//
//        LobbyUpdateMessage latestHealthyUpdate =
//                healthyCallback.lobbyUpdates.get(healthyCallback.lobbyUpdates.size() - 1);
//
//        assertEquals(2, latestHealthyUpdate.getConnectedPlayers());
//        assertEquals(3, latestHealthyUpdate.getRequiredPlayers());
//        assertEquals(List.of("alice", "bob"), latestHealthyUpdate.getPlayerNicknames());
//        assertFalse(latestHealthyUpdate.isGameStarting());
//    }
//
//    @Test
//    void onGameOver_shouldResetLobbyAndAllowANewMatchToStart() throws Exception {
//        // Arrange: fill a first 2-player lobby and let it start
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstMatchAlice = new RecordingCallback();
//        RecordingCallback firstMatchBob = new RecordingCallback();
//
//        lobbyController.registerPlayer("alice", 2, firstMatchAlice);
//        lobbyController.registerPlayer("bob", 2, firstMatchBob);
//
//        assertEquals(1, readyListener.readyCalls);
//        ServerController firstController = readyListener.lastController;
//        assertNotNull(firstController);
//
//        // Act: simulate game over, which should reset the lobby
//        lobbyController.onGameOver(firstController);
//
//        // Register a brand new first player with a different required size
//        RecordingCallback secondMatchCarol = new RecordingCallback();
//        lobbyController.registerPlayer("carol", 3, secondMatchCarol);
//
//        // Assert: after reset, the lobby must behave like a fresh one
//        assertEquals(1, secondMatchCarol.lobbyUpdates.size());
//
//        LobbyUpdateMessage update = secondMatchCarol.lobbyUpdates.get(0);
//        assertEquals(1, update.getConnectedPlayers());
//        assertEquals(3, update.getRequiredPlayers());
//        assertEquals(List.of("carol"), update.getPlayerNicknames());
//        assertFalse(update.isGameStarting());
//    }
//
//    @Test
//    void registerPlayer_shouldPassAUsableServerControllerToReadyListener() throws Exception {
//        // Arrange: create a 2-player lobby
//        RecordingLobbyReadyListener readyListener = new RecordingLobbyReadyListener();
//        LobbyController lobbyController = new LobbyController(readyListener);
//
//        RecordingCallback firstCallback = new RecordingCallback();
//        RecordingCallback secondCallback = new RecordingCallback();
//
//        // Act: fill the lobby
//        lobbyController.registerPlayer("alice", 2, firstCallback);
//        lobbyController.registerPlayer("bob", 2, secondCallback);
//
//        // Assert: the controller passed to the listener must contain both registered clients
//        assertNotNull(readyListener.lastController);
//        assertDoesNotThrow(() -> readyListener.lastController.getConnectionByNickname("alice"));
//        assertDoesNotThrow(() -> readyListener.lastController.getConnectionByNickname("bob"));
//    }
//}