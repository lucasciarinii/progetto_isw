//package it.polimi.ingsw.server.rmi;
//
//import it.polimi.ingsw.network.GameStateUpdateMessage;
//import it.polimi.ingsw.network.LobbyUpdateMessage;
//import it.polimi.ingsw.network.RankingUpdateMessage;
//import database.server.it.polimi.ingsw.RankingEntry;
//import enums.model.server.it.polimi.ingsw.Era;
//import enums.model.server.it.polimi.ingsw.GamePhase;
//import org.junit.jupiter.api.Test;
//
//import java.rmi.RemoteException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RMIClientConnectionTest {
//
//    /**
//     * Test double used to record every callback invocation performed
//     * by RMIClientConnection.
//     */
//    private static class RecordingCallback implements RMIClientCallback {
//        private GameStateUpdateMessage lastGameUpdate;
//        private String lastError;
//        private LobbyUpdateMessage lastLobbyUpdate;
//        private RankingUpdateMessage lastRankingUpdate;
//        private int shutdownCalls = 0;
//
//        @Override
//        public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
//            this.lastGameUpdate = update;
//        }
//
//        @Override
//        public void receiveError(String errorMessage) throws RemoteException {
//            this.lastError = errorMessage;
//        }
//
//        @Override
//        public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
//            this.lastLobbyUpdate = update;
//        }
//
//        @Override
//        public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
//            this.lastRankingUpdate = rankingUpdate;
//        }
//
//        @Override
//        public void receiveShutdown() throws RemoteException {
//            shutdownCalls++;
//        }
//    }
//
//    @Test
//    void sendUpdate_shouldForwardGameStateUpdateToCallback() throws Exception {
//        // Arrange: create a connection with a recording callback
//        RecordingCallback callback = new RecordingCallback();
//        RMIClientConnection connection = new RMIClientConnection(callback);
//
//        GameStateUpdateMessage update = new GameStateUpdateMessage(
//                1,
//                Era.I,
//                GamePhase.PLACE_TOTEMS,
//                "alice",
//                List.of("alice", "bob"),
//                List.of(),
//                List.of(),
//                List.of(),
//                List.of(),
//                List.of(),
//                List.of()
//        );
//
//        // Act: send the update through the adapter
//        connection.sendUpdate(update);
//
//        // Assert: the callback must receive the same update instance
//        assertSame(update, callback.lastGameUpdate);
//    }
//
//    @Test
//    void sendError_shouldForwardErrorToCallback() throws Exception {
//        // Arrange: create a connection with a recording callback
//        RecordingCallback callback = new RecordingCallback();
//        RMIClientConnection connection = new RMIClientConnection(callback);
//
//        // Act: send an error through the adapter
//        connection.sendError("Invalid move");
//
//        // Assert: the callback must receive the same error message
//        assertEquals("Invalid move", callback.lastError);
//    }
//
//    @Test
//    void sendRankingUpdate_shouldForwardRankingUpdateToCallback() throws Exception {
//        // Arrange: create a connection with a recording callback
//        RecordingCallback callback = new RecordingCallback();
//        RMIClientConnection connection = new RMIClientConnection(callback);
//
//        RankingUpdateMessage rankingUpdate = new RankingUpdateMessage(
//                List.of(new RankingEntry("alice", 3, 12.5)),
//                1
//        );
//
//        // Act: send the ranking update through the adapter
//        connection.sendRankingUpdate(rankingUpdate);
//
//        // Assert: the callback must receive the same ranking update instance
//        assertSame(rankingUpdate, callback.lastRankingUpdate);
//    }
//
//    @Test
//    void sendShutdown_shouldForwardShutdownToCallback() throws Exception {
//        // Arrange: create a connection with a recording callback
//        RecordingCallback callback = new RecordingCallback();
//        RMIClientConnection connection = new RMIClientConnection(callback);
//
//        // Act: send the shutdown signal through the adapter
//        connection.sendShutdown();
//
//        // Assert: the callback must be notified exactly once
//        assertEquals(1, callback.shutdownCalls);
//    }
//}