package org.example.server;

import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.board.OfferTile;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.GameState;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServerControllerTest {

    /**
     * Fake listener to verify if the controller correctly triggers the shutdown sequence.
     */
    private static class FakeGameOverListener implements GameOverListener {
        boolean called = false;
        @Override
        public void onGameOver(ServerController controller) {
            called = true;
        }
    }

    /**
     * Fake notifier used to capture everything that the server sends to the clients.
     * Expanded to track RoundFlow requests and Shutdown signals.
     */
    private static class FakeServerNotifier implements ServerNotifier {
        final Map<String, List<GameStateUpdateMessage>> gameUpdates = new HashMap<>();
        final Map<String, List<String>> errors = new HashMap<>();
        final Map<String, Integer> roundFlowRequests = new HashMap<>();
        final Map<String, Integer> shutdowns = new HashMap<>();

        @Override
        public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) {
            gameUpdates.computeIfAbsent(nickname, k -> new ArrayList<>()).add(update);
        }

        @Override
        public void sendError(String nickname, String message, GamePhase phase) {
            errors.computeIfAbsent(nickname, k -> new ArrayList<>()).add(message);
        }

        @Override
        public void sendRoundFlowCardRequest(String nickname) {
            roundFlowRequests.put(nickname, roundFlowRequests.getOrDefault(nickname, 0) + 1);
        }

        @Override
        public void sendShutdown(String nickname) {
            shutdowns.put(nickname, shutdowns.getOrDefault(nickname, 0) + 1);
        }

        // --- Other ServerNotifier methods (stubbed as empty for these tests) ---
        @Override public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) {}
        @Override public void sendRankingUpdate(String nickname, RankingUpdateMessage msg) {}
    }

    /** Match subclass used to force InvalidCardException from offerTileAction. */
    private static class InvalidCardMatch extends Match {
        InvalidCardMatch(List<Player> players) { super(players); }
        @Override
        public void offerTileAction(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
            throw new InvalidCardException("Bad cards");
        }
    }

    /** Match subclass used to force NoDrawableCardException from offerTileAction. */
    private static class NoDrawableCardMatch extends Match {
        NoDrawableCardMatch(List<Player> players) { super(players); }
        @Override
        public void offerTileAction(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
            throw new NoDrawableCardException("No drawable cards");
        }
    }

    /** Match subclass used to force InvalidCardException from roundFlowCardRequest. */
    private static class RoundFlowFailingMatch extends Match {
        RoundFlowFailingMatch(List<Player> players) { super(players); }
        @Override
        public void roundFlowCardRequest(Player player, String cards) throws InvalidCardException {
            throw new InvalidCardException("Bad flow card");
        }
    }

    /** Match subclass used to force a generic Exception from roundFlowCardRequest. */
    private static class RoundFlowGenericFailingMatch extends Match {
        RoundFlowGenericFailingMatch(List<Player> players) { super(players); }
        @Override
        public void roundFlowCardRequest(Player player, String cards) {
            throw new RuntimeException("Generic flow crash");
        }
    }

    /** Match subclass used to simulate a successful roundFlowCardRequest. */
    private static class RoundFlowSuccessMatch extends Match {
        RoundFlowSuccessMatch(List<Player> players) { super(players); }
        @Override
        public void roundFlowCardRequest(Player player, String cards) {
            // Success, does nothing and returns smoothly
        }
    }

    private FakeServerNotifier fakeNotifier;

    @BeforeEach
    void setUp() {
        fakeNotifier = new FakeServerNotifier();
    }

    // --- Helper Methods ---

    private Match createTwoPlayerMatch() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("alice"));
        players.add(new Player("bob"));
        return new Match(players);
    }

    private List<Player> createTwoPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("alice"));
        players.add(new Player("bob"));
        return players;
    }

    private Player findOtherPlayer(Match match) {
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        for (Player player : match.getPlayers()) {
            if (!player.getNickname().equals(currentNickname)) {
                return player;
            }
        }
        throw new IllegalStateException("Other player not found");
    }

    private void moveToPlayerTurn(Match match) {
        if (match.getGameState().getCurrentPhase() == GamePhase.PLACE_TOTEMS) {
            match.getGameState().advancePhase();
        }
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
    }

    // --- Original Standard Tests ---

    @Test
    void sendInitialState_ShouldSendSnapshotToAllPlayers() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);
        assertDoesNotThrow(controller::sendInitialState);
        assertEquals(1, fakeNotifier.gameUpdates.get("alice").size());
        assertEquals(1, fakeNotifier.gameUpdates.get("bob").size());
    }

    @Test
    void placeTotemOnOfferTile_ShouldSendErrorWhenPlayerIsNotCurrentPlayer() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);
        String otherNickname = findOtherPlayer(match).getNickname();
        controller.placeTotemOnOfferTile(otherNickname, 1);
        List<String> errorsForOther = fakeNotifier.errors.get(otherNickname);
        assertNotNull(errorsForOther);
        assertTrue(errorsForOther.get(0).contains("not yourn turn or invalid phase"));
    }

    @Test
    void skipTurn_ShouldSendErrorWhenPhaseIsWrong() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        controller.skipTurn(currentNickname);
        assertTrue(fakeNotifier.errors.get(currentNickname).get(0).contains("invalid phase"));
    }

    @Test
    void skipTurn_ShouldAdvanceTurnAndNotifyClientsWhenPhaseIsPlayerTurn() {
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();
        controller.skipTurn(currentNickname);
        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());
    }

    @Test
    void offerTileAction_ShouldSendErrorWhenMatchThrowsInvalidCardException() {
        InvalidCardMatch match = new InvalidCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        controller.offerTileAction(currentNickname, "1,2");
        assertTrue(fakeNotifier.errors.get(currentNickname).get(0).contains("Invalid move: Bad cards"));
    }

    @Test
    void offerTileAction_ShouldRecoverWhenMatchThrowsNoDrawableCardException() {
        NoDrawableCardMatch match = new NoDrawableCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);
        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        OfferTile selectedTile = match.getBoard().getOfferTrack().get(0);
        selectedTile.placePlayer(match.getGameState().getCurrentPlayer());

        controller.offerTileAction(currentNickname, "");
        assertTrue(fakeNotifier.errors.get(currentNickname).get(0).contains("No drawable cards"));
        assertNull(selectedTile.getPlayer());
    }

    // --- NEW COVERAGE: RoundFlowCardRequest logic ---

    @Test
    void roundFlowCardRequest_ShouldSendErrorIfNotWaiting() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        // Try to answer round flow while the controller is not waiting
        controller.roundFlowCardRequest("alice", "1");

        List<String> errors = fakeNotifier.errors.get("alice");
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("No RoundFlow action expected now"));
    }

    @Test
    void roundFlowCardRequest_ShouldSendErrorIfWrongPlayer() throws Exception {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        // Force internal state using Reflection to simulate waiting for Alice
        Field waitField = ServerController.class.getDeclaredField("waitingForRoundFlow");
        waitField.setAccessible(true);
        waitField.set(controller, true);

        Field nickField = ServerController.class.getDeclaredField("roundFlowPlayerNick");
        nickField.setAccessible(true);
        nickField.set(controller, "alice");

        // Bob tries to answer Alice's request
        controller.roundFlowCardRequest("bob", "1");

        List<String> errors = fakeNotifier.errors.get("bob");
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("It's not your RoundFlow turn"));
    }

    @Test
    void roundFlowCardRequest_ShouldHandleInvalidCardException() throws Exception {
        RoundFlowFailingMatch match = new RoundFlowFailingMatch(createTwoPlayers());
        ServerController controller = new ServerController(match, fakeNotifier);

        Field waitField = ServerController.class.getDeclaredField("waitingForRoundFlow");
        waitField.setAccessible(true); waitField.set(controller, true);
        Field nickField = ServerController.class.getDeclaredField("roundFlowPlayerNick");
        nickField.setAccessible(true); nickField.set(controller, "alice");

        // Alice sends a bad card, triggering InvalidCardException
        controller.roundFlowCardRequest("alice", "bad_input");

        assertTrue(fakeNotifier.errors.get("alice").get(0).contains("Invalid move: Bad flow card"));
        // The server should automatically send a new request to Alice to try again
        assertEquals(1, fakeNotifier.roundFlowRequests.get("alice"));
    }

    @Test
    void roundFlowCardRequest_ShouldHandleGenericException() throws Exception {
        RoundFlowGenericFailingMatch match = new RoundFlowGenericFailingMatch(createTwoPlayers());
        ServerController controller = new ServerController(match, fakeNotifier);

        Field waitField = ServerController.class.getDeclaredField("waitingForRoundFlow");
        waitField.setAccessible(true); waitField.set(controller, true);
        Field nickField = ServerController.class.getDeclaredField("roundFlowPlayerNick");
        nickField.setAccessible(true); nickField.set(controller, "alice");

        controller.roundFlowCardRequest("alice", "error_input");

        assertTrue(fakeNotifier.errors.get("alice").get(0).contains("Generic Exception: Generic flow crash"));
        assertEquals(1, fakeNotifier.roundFlowRequests.get("alice"));
    }

    @Test
    void roundFlowCardRequest_ShouldCompleteSuccessfullyAndProceed() throws Exception {
        RoundFlowSuccessMatch match = new RoundFlowSuccessMatch(createTwoPlayers());
        ServerController controller = new ServerController(match, fakeNotifier);

        // --- FIX: Place players on the offer track ---
        // This ensures that when the round ends, proceedEndRound() can calculate
        // the new turn order correctly without creating an empty list!
        match.getBoard().getOfferTrack().get(0).placePlayer(match.getPlayers().get(0));
        match.getBoard().getOfferTrack().get(1).placePlayer(match.getPlayers().get(1));

        Field waitField = ServerController.class.getDeclaredField("waitingForRoundFlow");
        waitField.setAccessible(true); waitField.set(controller, true);
        Field nickField = ServerController.class.getDeclaredField("roundFlowPlayerNick");
        nickField.setAccessible(true); nickField.set(controller, "alice");

        // Act: Alice completes the request successfully
        controller.roundFlowCardRequest("alice", "good_input");

        // Assert: The wait flags must be cleared
        assertFalse((boolean) waitField.get(controller));
        assertNull(nickField.get(controller));

        // Controller must notify all clients about the progression to the next phase/round
        assertTrue(fakeNotifier.gameUpdates.containsKey("alice"));
        assertTrue(fakeNotifier.gameUpdates.containsKey("bob"));
    }
    @Test
    void gamePhases_ShouldTriggerEndGameAndShutdownOnRound10() throws Exception {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        FakeGameOverListener listener = new FakeGameOverListener();
        controller.setGameOverListener(listener);

        // Use reflection to forcefully jump to round 10 to trigger the end game sequence
        Field roundField = GameState.class.getDeclaredField("currentRound");
        roundField.setAccessible(true);
        roundField.set(match.getGameState(), 10);

        moveToPlayerTurn(match);

        // Let players skip their turn. The last skip will trigger EVENTS_RESOLVE -> END_ROUND -> END_GAME
        String first = match.getGameState().getCurrentPlayer().getNickname();
        controller.skipTurn(first);
        String second = match.getGameState().getCurrentPlayer().getNickname();
        controller.skipTurn(second);

        // Assert that the phase automatically trickled down to GAME_OVER
        assertEquals(GamePhase.GAME_OVER, match.getGameState().getCurrentPhase());

        // Verify the listener caught the shutdown sequence
        assertTrue(listener.called);

        // Verify that the shutdown signal was sent to all clients
        assertEquals(1, fakeNotifier.shutdowns.get("alice"));
        assertEquals(1, fakeNotifier.shutdowns.get("bob"));
    }
}