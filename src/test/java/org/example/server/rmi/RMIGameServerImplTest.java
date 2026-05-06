package org.example.server.rmi;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.ServerController;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RMIGameServerImplTest {

    private RMIGameServerImpl server;

    @AfterEach
    void tearDown() throws Exception {
        // Unexport the remote object created by UnicastRemoteObject to avoid leaking RMI state across tests.
        if (server != null) {
            UnicastRemoteObject.unexportObject(server, true);
        }
    }

    /**
     * Recording callback used to observe lobby-side notifications and optionally fail on receiveError.
     */
    private static class RecordingCallback implements RMIClientCallback {
        private LobbyUpdateMessage lastLobbyUpdate;
        private String lastError;
        private boolean throwOnReceiveError;

        RecordingCallback() {
            this(false);
        }

        RecordingCallback(boolean throwOnReceiveError) {
            this.throwOnReceiveError = throwOnReceiveError;
        }

        @Override
        public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
            // Not needed in these tests.
        }

        @Override
        public void receiveError(String errorMessage) throws RemoteException {
            if (throwOnReceiveError) {
                throw new RemoteException("callback failed");
            }
            this.lastError = errorMessage;
        }

        @Override
        public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
            this.lastLobbyUpdate = update;
        }

        @Override
        public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
            // Not needed in these tests.
        }

        @Override
        public void receiveShutdown() throws RemoteException {
            // Not needed in these tests.
        }
    }

    /**
     * Spy controller used to verify that RMIGameServerImpl delegates calls once the game has started.
     */
    private static class SpyServerController extends ServerController {
        private String placeTotemNickname;
        private Integer placeTotemTilePosition;
        private String offerActionNickname;
        private String offerActionCards;
        private String skippedNickname;

        SpyServerController() {
            super(new Match(createPlayers(2)));
        }

        @Override
        public void placeTotemOnOfferTile(String nickname, int tilePosition) {
            this.placeTotemNickname = nickname;
            this.placeTotemTilePosition = tilePosition;
        }

        @Override
        public void offerTileAction(String nickname, String cards) {
            this.offerActionNickname = nickname;
            this.offerActionCards = cards;
        }

        @Override
        public void skipTurn(String nickname) {
            this.skippedNickname = nickname;
        }
    }

    private static List<Player> createPlayers(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> new Player("player" + i))
                .toList();
    }

    @Test
    void register_shouldDelegateToLobbyAndSendWaitingLobbyUpdate() throws Exception {
        // Arrange: create the RMI server and a callback for the first player.
        server = new RMIGameServerImpl();
        RecordingCallback callback = new RecordingCallback();

        // Act: register the first player with a valid lobby size.
        assertDoesNotThrow(() -> server.register("alice", 2, callback));

        // Assert: the player must receive the waiting-lobby update.
        assertNotNull(callback.lastLobbyUpdate);
        assertEquals(1, callback.lastLobbyUpdate.getConnectedPlayers());
        assertEquals(2, callback.lastLobbyUpdate.getRequiredPlayers());
        assertEquals(List.of("alice"), callback.lastLobbyUpdate.getPlayerNicknames());
        assertFalse(callback.lastLobbyUpdate.isGameStarting());
    }

    @Test
    void register_shouldWrapLobbyExceptionsAsRemoteException() throws Exception {
        // Arrange: register a valid player first, then reuse the same nickname with a callback
        // that fails when LobbyController tries to notify the duplicate-nickname error.
        server = new RMIGameServerImpl();
        server.register("alice", 2, new RecordingCallback());
        RecordingCallback failingCallback = new RecordingCallback(true);

        // Act + Assert: the server must wrap the propagated lobby-side exception.
        RemoteException ex = assertThrows(RemoteException.class,
                () -> server.register("alice", 2, failingCallback));

        assertTrue(ex.getMessage().contains("callback failed"));
    }

    @Test
    void placeTotemOnOfferTile_shouldThrowWhenGameNotStarted() throws Exception {
        // Arrange: create the server without a ready ServerController.
        server = new RMIGameServerImpl();

        // Act + Assert: actions must be rejected before the lobby starts the match.
        RemoteException ex = assertThrows(RemoteException.class,
                () -> server.placeTotemOnOfferTile("alice", 1));

        assertTrue(ex.getMessage().contains("not started"));
    }

    @Test
    void offerTileAction_shouldThrowWhenGameNotStarted() throws Exception {
        // Arrange: create the server without a ready ServerController.
        server = new RMIGameServerImpl();

        // Act + Assert: actions must be rejected before the lobby starts the match.
        RemoteException ex = assertThrows(RemoteException.class,
                () -> server.offerTileAction("alice", "1,2"));

        assertTrue(ex.getMessage().contains("not started"));
    }

    @Test
    void skipTurn_shouldThrowWhenGameNotStarted() throws Exception {
        // Arrange: create the server without a ready ServerController.
        server = new RMIGameServerImpl();

        // Act + Assert: actions must be rejected before the lobby starts the match.
        RemoteException ex = assertThrows(RemoteException.class,
                () -> server.skipTurn("alice"));

        assertTrue(ex.getMessage().contains("not started"));
    }

    @Test
    void placeTotemOnOfferTile_shouldDelegateToServerControllerAfterLobbyReady() throws Exception {
        // Arrange: make the server ready with a spy controller.
        server = new RMIGameServerImpl();
        SpyServerController controller = new SpyServerController();
        server.onLobbyReady(controller);

        // Act: invoke the RMI entrypoint.
        server.placeTotemOnOfferTile("alice", 3);

        // Assert: the request must be forwarded to the controller with the same parameters.
        assertEquals("alice", controller.placeTotemNickname);
        assertEquals(3, controller.placeTotemTilePosition);
    }

    @Test
    void offerTileAction_shouldDelegateToServerControllerAfterLobbyReady() throws Exception {
        // Arrange: make the server ready with a spy controller.
        server = new RMIGameServerImpl();
        SpyServerController controller = new SpyServerController();
        server.onLobbyReady(controller);

        // Act: invoke the RMI entrypoint.
        server.offerTileAction("alice", "4,5");

        // Assert: the request must be forwarded to the controller with the same parameters.
        assertEquals("alice", controller.offerActionNickname);
        assertEquals("4,5", controller.offerActionCards);
    }

    @Test
    void skipTurn_shouldDelegateToServerControllerAfterLobbyReady() throws Exception {
        // Arrange: make the server ready with a spy controller.
        server = new RMIGameServerImpl();
        SpyServerController controller = new SpyServerController();
        server.onLobbyReady(controller);

        // Act: invoke the RMI entrypoint.
        server.skipTurn("alice");

        // Assert: the request must be forwarded to the controller with the same nickname.
        assertEquals("alice", controller.skippedNickname);
    }
}