package org.example.client;

import org.example.client.view.CLIInputHandler;
import org.example.client.view.View;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;
import org.example.server.rmi.RMIGameServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private ClientController clientController;

    @Mock
    private RMIGameServer mockServer;

    @Mock
    private View mockView;

    @Mock
    private CLIInputHandler mockInputHandler;

    private final String TEST_NICKNAME = "TestPlayer";

    @BeforeEach
    void setUp() throws Exception {
        clientController = new ClientController(TEST_NICKNAME);

        // Safe injection of mocks using Java Reflection
        injectMock("server", mockServer);
        injectMock("view", mockView);
        injectMock("inputHandler", mockInputHandler);
    }

    private void injectMock(String fieldName, Object mockObject) throws Exception {
        Field field = ClientController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(clientController, mockObject);
    }

    // ==========================================
    // GETTERS AND BASIC METHODS TESTS
    // ==========================================

    /**
     * What we are testing: Verifies that getNickname correctly returns the nickname passed during initialization.
     */
    @Test
    void testGetNickname() {
        assertEquals(TEST_NICKNAME, clientController.getNickname());
    }

    /**
     * What we are testing: Verifies that getView correctly returns the View instance managed by the controller.
     */
    @Test
    void testGetView() {
        assertEquals(mockView, clientController.getView());
    }

    /**
     * What we are testing: Verifies that when the client decides to place a totem,
     * the command is forwarded exactly once to the RMI server stub with the correct parameters.
     */
    @Test
    void testPlaceTotemOnOfferTile() throws Exception {
        int tilePosition = 3;
        clientController.placeTotemOnOfferTile(tilePosition);
        Mockito.verify(mockServer, Mockito.times(1)).placeTotemOnOfferTile(TEST_NICKNAME, tilePosition);
    }

    /**
     * What we are testing: Verifies that when the client sends a card selection string,
     * the command is forwarded exactly once to the RMI server stub with the correct parameters.
     */
    @Test
    void testOfferTileAction() throws Exception {
        String cardsInput = "1,2";
        clientController.offerTileAction(cardsInput);
        Mockito.verify(mockServer, Mockito.times(1)).offerTileAction(TEST_NICKNAME, cardsInput);
    }

    /**
     * What we are testing: Verifies that when the application shuts down,
     * the controller correctly delegates the exit warning to the CLI input handler.
     */
    @Test
    void testOnShutdown() {
        clientController.onShutdown();
        Mockito.verify(mockInputHandler, Mockito.times(1)).warnExit();
    }

    // ==========================================
    // LOBBY TESTS
    // ==========================================

    /**
     * What we are testing: Verifies that the controller handles the "game starting" lobby update
     * without throwing exceptions (it just prints to the console in the current implementation).
     */
    @Test
    void testOnLobbyUpdate_GameStarting() {
        LobbyUpdateMessage mockMessage = Mockito.mock(LobbyUpdateMessage.class);
        Mockito.when(mockMessage.isGameStarting()).thenReturn(true);

        clientController.onLobbyUpdate(mockMessage);
    }

    /**
     * What we are testing: Verifies that the controller handles standard lobby updates (players joining)
     * without throwing exceptions, correctly reading the connected players data.
     */
    @Test
    void testOnLobbyUpdate_GameNotStarting() {
        LobbyUpdateMessage mockMessage = Mockito.mock(LobbyUpdateMessage.class);
        Mockito.when(mockMessage.isGameStarting()).thenReturn(false);
        Mockito.when(mockMessage.getConnectedPlayers()).thenReturn(2);
        Mockito.when(mockMessage.getRequiredPlayers()).thenReturn(4);
        Mockito.when(mockMessage.getPlayerNicknames()).thenReturn(List.of("P1", "P2"));

        clientController.onLobbyUpdate(mockMessage);
    }

    // ==========================================
    // ERROR AND RANKING TESTS
    // ==========================================

    /**
     * What we are testing: Verifies that when an error string is received from the server,
     * the controller displays the error in the View and prompts the user for action again.
     */
    @Test
    void testOnError() {
        String errorMessage = "Invalid move!";
        Mockito.when(mockView.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        clientController.onError(errorMessage);

        Mockito.verify(mockView, Mockito.times(1)).displayError(errorMessage);
        Mockito.verify(mockInputHandler, Mockito.times(1)).promptForAction(GamePhase.PLAYER_TURN);
    }

    /**
     * What we are testing: Verifies that a ranking update message is correctly processed
     * and delegated to the View to be displayed.
     */
    @Test
    void testOnRankingUpdate() {
        RankingUpdateMessage mockMessage = Mockito.mock(RankingUpdateMessage.class);
        List mockRanking = Mockito.mock(List.class);
        Mockito.doReturn(mockRanking).when(mockMessage).getRanking();
        Mockito.when(mockMessage.getPlayerRankPosition()).thenReturn(2);

        clientController.onRankingUpdate(mockMessage);

        Mockito.verify(mockView, Mockito.times(1)).displayRankingUpdate(mockRanking, 2);
    }

    // ==========================================
    // ON UPDATE (TURN LOGIC) TESTS
    // ==========================================

    /**
     * What we are testing: Verifies behavior when an update arrives, it's NOT the player's turn,
     * but it IS an interactive phase. The View should be updated and display a "waiting for..." message,
     * and the user should NOT be prompted for input.
     */
    @Test
    void testOnUpdate_NotMyTurn_InteractivePhase() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn("AnotherPlayer");
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockView, Mockito.times(1)).update(mockUpdate);
        Mockito.verify(mockView, Mockito.times(1)).displayWaiting("AnotherPlayer");
        Mockito.verify(mockInputHandler, Mockito.never()).promptForAction(ArgumentMatchers.any());
    }

    /**
     * What we are testing: Verifies behavior when an update arrives and it's an automatic phase
     * (e.g., END_ROUND). The View is updated, but no "waiting" message should be displayed.
     */
    @Test
    void testOnUpdate_NotMyTurn_NotInteractivePhase() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn("AnotherPlayer");
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.END_ROUND);

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockView, Mockito.times(1)).update(mockUpdate);
        Mockito.verify(mockView, Mockito.never()).displayWaiting(ArgumentMatchers.anyString());
    }

    /**
     * What we are testing: Verifies that during the PLACE_TOTEMS phase, if it is the player's turn,
     * the controller directly prompts the user for action without checking for pickable cards.
     */
    @Test
    void testOnUpdate_MyTurn_PlaceTotems() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLACE_TOTEMS);

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockInputHandler, Mockito.times(1)).promptForAction(GamePhase.PLACE_TOTEMS);
    }

    /**
     * What we are testing: Verifies that during PLAYER_TURN on a FOOD offer tile,
     * the client bypasses the pickable cards check (since food is always pickable) and prompts the user.
     */
    @Test
    void testOnUpdate_MyTurn_PlayerTurn_FoodEffect_AlwaysPickable() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        OfferTileSnapshot tile = Mockito.mock(OfferTileSnapshot.class);
        Mockito.when(tile.getOccupantNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(tile.getOfferEffect()).thenReturn(OfferEffect.FOOD);
        Mockito.when(mockUpdate.getOfferTrack()).thenReturn(List.of(tile));

        PlayerSnapshot pSnap = Mockito.mock(PlayerSnapshot.class);
        Mockito.when(pSnap.getNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getPlayers()).thenReturn(List.of(pSnap));

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockInputHandler, Mockito.times(1)).promptForAction(GamePhase.PLAYER_TURN);
    }

    /**
     * What we are testing: Verifies that during PLAYER_TURN on an offer tile that requires a card pick (e.g., 'D'),
     * if there is at least one Character card available (which is always pickable), the user is prompted.
     */
    @Test
    void testOnUpdate_MyTurn_PlayerTurn_CharacterCard_IsPickable() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        OfferTileSnapshot tile = Mockito.mock(OfferTileSnapshot.class);
        Mockito.when(tile.getOccupantNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(tile.getOfferEffect()).thenReturn(OfferEffect.D);
        Mockito.when(mockUpdate.getOfferTrack()).thenReturn(List.of(tile));

        PlayerSnapshot pSnap = Mockito.mock(PlayerSnapshot.class);
        Mockito.when(pSnap.getNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getPlayers()).thenReturn(List.of(pSnap));

        Card charCard = Mockito.mock(Card.class);
        Mockito.when(charCard.isCharacter()).thenReturn(true);
        Mockito.when(mockUpdate.getBottomRow()).thenReturn(List.of(charCard));

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockInputHandler, Mockito.times(1)).promptForAction(GamePhase.PLAYER_TURN);
    }

    /**
     * What we are testing: Verifies that if the player lands on a tile requiring a pick,
     * but the only available card is a Building they cannot afford, the client automatically
     * skips the turn and notifies the user via the View.
     */
    @Test
    void testOnUpdate_MyTurn_PlayerTurn_ExpensiveBuilding_NotPickable_SkipTurn() throws Exception {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        OfferTileSnapshot tile = Mockito.mock(OfferTileSnapshot.class);
        Mockito.when(tile.getOccupantNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(tile.getOfferEffect()).thenReturn(OfferEffect.D);
        Mockito.when(mockUpdate.getOfferTrack()).thenReturn(List.of(tile));

        PlayerSnapshot pSnap = Mockito.mock(PlayerSnapshot.class);
        Mockito.when(pSnap.getNickname()).thenReturn(TEST_NICKNAME);
        // Player has 2 food + 1 discount = 3 effective buying power
        Mockito.when(pSnap.getFood()).thenReturn(2);
        Mockito.when(pSnap.getDiscountOnBuilding()).thenReturn(1);
        Mockito.when(mockUpdate.getPlayers()).thenReturn(List.of(pSnap));

        // Expensive building: costs 10 food
        BuildingCard expensiveBuilding = Mockito.mock(BuildingCard.class);
        Mockito.when(expensiveBuilding.isCharacter()).thenReturn(false);
        Mockito.when(expensiveBuilding.isBuilding()).thenReturn(true);
        Mockito.when(expensiveBuilding.getFoodCost()).thenReturn(10);
        Mockito.when(mockUpdate.getBottomRow()).thenReturn(List.of(expensiveBuilding));

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockView, Mockito.times(1)).displayNoCardsPickable();
        Mockito.verify(mockServer, Mockito.times(1)).skipTurn(TEST_NICKNAME);
        Mockito.verify(mockInputHandler, Mockito.never()).promptForAction(ArgumentMatchers.any());
    }

    /**
     * What we are testing: Verifies the edge case where the client automatically tries to skip a turn
     * due to unpickable cards, but the RMI connection fails (RemoteException). The error must be displayed.
     */
    @Test
    void testOnUpdate_MyTurn_PlayerTurn_SkipTurn_ThrowsRemoteException() throws Exception {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        OfferTileSnapshot tile = Mockito.mock(OfferTileSnapshot.class);
        Mockito.when(tile.getOccupantNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(tile.getOfferEffect()).thenReturn(OfferEffect.D);
        Mockito.when(mockUpdate.getOfferTrack()).thenReturn(List.of(tile));

        PlayerSnapshot pSnap = Mockito.mock(PlayerSnapshot.class);
        Mockito.when(pSnap.getNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getPlayers()).thenReturn(List.of(pSnap));

        Mockito.when(mockUpdate.getBottomRow()).thenReturn(List.of());

        // Simulate a network failure during the automatic skipTurn call
        Mockito.doThrow(new RemoteException("Connection lost")).when(mockServer).skipTurn(TEST_NICKNAME);

        clientController.onUpdate(mockUpdate);

        Mockito.verify(mockView, Mockito.times(1)).displayError(ArgumentMatchers.contains("Communication error: Connection lost"));
    }

    /**
     * What we are testing: Verifies that an IllegalArgumentException is thrown if the current player's data
     * cannot be found in the state update (which indicates a severe state inconsistency).
     */
    @Test
    void testOnUpdate_MyTurn_PlayerTurn_PlayerNotFound() {
        GameStateUpdateMessage mockUpdate = Mockito.mock(GameStateUpdateMessage.class);
        Mockito.when(mockUpdate.getCurrentPlayerNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(mockUpdate.getCurrentPhase()).thenReturn(GamePhase.PLAYER_TURN);

        OfferTileSnapshot tile = Mockito.mock(OfferTileSnapshot.class);
        Mockito.when(tile.getOccupantNickname()).thenReturn(TEST_NICKNAME);
        Mockito.when(tile.getOfferEffect()).thenReturn(OfferEffect.FOOD);
        Mockito.when(mockUpdate.getOfferTrack()).thenReturn(List.of(tile));

        Mockito.when(mockUpdate.getPlayers()).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> clientController.onUpdate(mockUpdate));
        assertEquals("Player not found: " + TEST_NICKNAME, exception.getMessage());
    }

    // ==========================================
    // CONNECTION TEST (STATIC MOCKING)
    // ==========================================

    /**
     * What we are testing:
     * We want to verify that the 'connect' method properly sets the system property for RMI,
     * correctly performs a lookup to find the server, and successfully registers the client
     * to the server using the provided nickname and player count.
     */
    @Test
    void testConnect() throws Exception {
        String host = "127.0.0.1";
        int numPlayers = 3;

        // 1. Create an "interceptor" (mock) for the static class java.rmi.Naming
        try (org.mockito.MockedStatic<java.rmi.Naming> mockedNaming = Mockito.mockStatic(java.rmi.Naming.class)) {

            // 2. Tell the interceptor: if someone calls lookup() with this exact URL, return our mockServer
            mockedNaming.when(() -> java.rmi.Naming.lookup("rmi://" + host + "/GameServer"))
                    .thenReturn(mockServer);

            // Act: call the real 'connect' method on the controller
            clientController.connect(host, numPlayers);

            // Assert 1: verify that the system property was correctly set
            assertEquals("localhost", System.getProperty("java.rmi.server.hostname"));

            // Assert 2: verify that the server's 'register' method was called exactly once with the right parameters.
            // Since the 'callback' object is created with 'new' inside the method, we use ArgumentMatchers.any()
            // to say "any instance of RMIClientCallbackImpl is acceptable".
            Mockito.verify(mockServer, Mockito.times(1)).register(
                    ArgumentMatchers.eq(TEST_NICKNAME),
                    ArgumentMatchers.eq(numPlayers),
                    ArgumentMatchers.any(org.example.client.rmi.RMIClientCallbackImpl.class)
            );
        }
    }
}