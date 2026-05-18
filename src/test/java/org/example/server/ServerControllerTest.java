package org.example.server;

import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.board.OfferTile;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServerControllerTest {

    /**
     * Fake notifier used to capture everything that the server sends to the clients.
     * It intercepts network calls and stores them in lists for verification.
     */
    private static class FakeServerNotifier implements ServerNotifier {
        final Map<String, List<GameStateUpdateMessage>> gameUpdates = new HashMap<>();
        final Map<String, List<String>> errors = new HashMap<>();

        @Override
        public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) {
            gameUpdates.computeIfAbsent(nickname, k -> new ArrayList<>()).add(update);
        }

        @Override
        public void sendError(String nickname, String message, GamePhase phase) {
            errors.computeIfAbsent(nickname, k -> new ArrayList<>()).add(message);
        }

        // --- Other ServerNotifier methods (stubbed as empty for these tests) ---
        @Override public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) {}
        @Override public void sendRoundFlowCardRequest(String nickname) {}
        @Override public void sendRankingUpdate(String nickname, RankingUpdateMessage msg) {}
        @Override public void sendShutdown(String nickname) {}
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
     * Match subclass used to force an exception during placeTotemOnOfferTile.
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

    /**
     * Match subclass used to force a generic runtime exception from offerTileAction.
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

    // --- Tests ---

    @Test
    void sendInitialState_ShouldSendSnapshotToAllPlayers() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        // Act
        assertDoesNotThrow(controller::sendInitialState);

        // Assert
        assertEquals(1, fakeNotifier.gameUpdates.get("alice").size());
        assertEquals(1, fakeNotifier.gameUpdates.get("bob").size());
    }

    @Test
    void placeTotemOnOfferTile_ShouldSendErrorWhenPlayerIsNotCurrentPlayer() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        // Act
        controller.placeTotemOnOfferTile(otherNickname, 1);

        // Assert
        List<String> errorsForOther = fakeNotifier.errors.get(otherNickname);
        assertNotNull(errorsForOther);
        assertEquals(1, errorsForOther.size());
        assertTrue(errorsForOther.get(0).contains("not yourn turn or invalid phase"));

        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    @Test
    void placeTotemOnOfferTile_ShouldAdvanceTurnAndNotifyClientsWhenMoveIsValid() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        // Act
        controller.placeTotemOnOfferTile(currentNickname, 1);

        // Assert
        OfferTile tile = match.getBoard().getOfferTrack().get(0);
        assertNotNull(tile.getPlayer());
        assertEquals(currentNickname, tile.getPlayer().getNickname());

        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());

        assertEquals(1, fakeNotifier.gameUpdates.get(currentNickname).size());
        assertEquals(1, fakeNotifier.gameUpdates.get(otherNickname).size());

        GameStateUpdateMessage updateForCurrent = fakeNotifier.gameUpdates.get(currentNickname).get(0);
        assertEquals(otherNickname, updateForCurrent.getCurrentPlayerNickname());
    }

    @Test
    void skipTurn_ShouldSendErrorWhenPhaseIsWrong() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act
        controller.skipTurn(currentNickname);

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("invalid phase"));
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    @Test
    void skipTurn_ShouldAdvanceTurnAndNotifyClientsWhenPhaseIsPlayerTurn() {
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        // Act
        controller.skipTurn(currentNickname);

        // Assert
        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());

        assertEquals(1, fakeNotifier.gameUpdates.get(currentNickname).size());
        assertEquals(1, fakeNotifier.gameUpdates.get(otherNickname).size());
        assertEquals(otherNickname, fakeNotifier.gameUpdates.get(currentNickname).get(0).getCurrentPlayerNickname());
    }

    @Test
    void offerTileAction_ShouldSendErrorWhenMatchThrowsInvalidCardException() {
        InvalidCardMatch match = new InvalidCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act
        controller.offerTileAction(currentNickname, "1,2");

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Invalid move: Bad cards"));
        assertEquals(currentNickname, match.getGameState().getCurrentPlayer().getNickname());
    }

    @Test
    void offerTileAction_ShouldRecoverWhenMatchThrowsNoDrawableCardException() {
        NoDrawableCardMatch match = new NoDrawableCardMatch(createTwoPlayers());
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();
        String otherNickname = findOtherPlayer(match).getNickname();

        // Manually place the current player on an offer tile to test recovery branch
        OfferTile selectedTile = match.getBoard().getOfferTrack().get(0);
        selectedTile.placePlayer(match.getGameState().getCurrentPlayer());

        // Act
        controller.offerTileAction(currentNickname, "");

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("No drawable cards"));

        assertNull(selectedTile.getPlayer());
        assertEquals(otherNickname, match.getGameState().getCurrentPlayer().getNickname());

        assertEquals(1, fakeNotifier.gameUpdates.get(currentNickname).size());
        assertEquals(1, fakeNotifier.gameUpdates.get(otherNickname).size());
    }

    @Test
    void offerTileAction_ShouldSendErrorWhenWrongPlayer() {
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String otherNickname = findOtherPlayer(match).getNickname();

        // Act
        controller.offerTileAction(otherNickname, "");

        // Assert
        List<String> errors = fakeNotifier.errors.get(otherNickname);
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("not yourn turn"));
    }

    @Test
    void offerTileAction_ShouldSendErrorWhenWrongPhase() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act
        controller.offerTileAction(currentNickname, "");

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("invalid phase"));
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
    }

    @Test
    void offerTileAction_ShouldSendErrorWhenGenericExceptionOccurs() {
        GenericFailureMatch match = new GenericFailureMatch(createTwoPlayers());
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act
        controller.offerTileAction(currentNickname, "1");

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("Generic Exception: Boom"));
    }

    @Test
    void placeTotemOnOfferTile_ShouldSendErrorWhenModelThrowsException() {
        FailingPlaceTotemMatch match = new FailingPlaceTotemMatch(createTwoPlayers());
        ServerController controller = new ServerController(match, fakeNotifier);

        String currentNickname = match.getGameState().getCurrentPlayer().getNickname();

        // Act
        controller.placeTotemOnOfferTile(currentNickname, 99);

        // Assert
        List<String> errors = fakeNotifier.errors.get(currentNickname);
        assertNotNull(errors);
        assertTrue(errors.get(0).contains("Invalid move: Invalid tile"));
    }

    @Test
    void placeTotemOnOfferTile_LastPlayerShouldSwitchPhaseToPlayerTurnAndNotifyOrder() {
        Match match = createTwoPlayerMatch();
        ServerController controller = new ServerController(match, fakeNotifier);

        String firstNickname = match.getGameState().getCurrentPlayer().getNickname();
        String secondNickname = findOtherPlayer(match).getNickname();

        // Act
        controller.placeTotemOnOfferTile(firstNickname, 1);
        controller.placeTotemOnOfferTile(secondNickname, 2);

        // Assert
        assertEquals(GamePhase.PLAYER_TURN, match.getGameState().getCurrentPhase());
        assertEquals(firstNickname, match.getGameState().getCurrentPlayer().getNickname());

        List<GameStateUpdateMessage> updates = fakeNotifier.gameUpdates.get(firstNickname);
        GameStateUpdateMessage lastUpdate = updates.get(updates.size() - 1);

        assertEquals(GamePhase.PLAYER_TURN, lastUpdate.getCurrentPhase());
        assertEquals(firstNickname, lastUpdate.getCurrentPlayerNickname());
        assertEquals(List.of(firstNickname, secondNickname), lastUpdate.getTurnOrder());
    }

    @Test
    void skipTurn_LastPlayerShouldAdvanceThroughEndRoundToNextRound() {
        Match match = createTwoPlayerMatch();
        moveToPlayerTurn(match);
        ServerController controller = new ServerController(match, fakeNotifier);

        String firstNickname = match.getGameState().getCurrentPlayer().getNickname();
        String secondNickname = findOtherPlayer(match).getNickname();

        // Act: Both players skip, pushing the phase to the next round
        controller.skipTurn(firstNickname);
        controller.skipTurn(secondNickname);

        // Assert
        assertEquals(GamePhase.PLACE_TOTEMS, match.getGameState().getCurrentPhase());
        assertEquals(2, match.getGameState().getCurrentRound());

        List<GameStateUpdateMessage> updates = fakeNotifier.gameUpdates.get(firstNickname);
        GameStateUpdateMessage lastUpdate = updates.get(updates.size() - 1);

        assertEquals(GamePhase.PLACE_TOTEMS, lastUpdate.getCurrentPhase());
        assertEquals(2, lastUpdate.getCurrentRound());
    }
}