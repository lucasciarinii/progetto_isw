package it.polimi.ingsw.client;

import it.polimi.ingsw.client.view.UIHandler;
import it.polimi.ingsw.network.ClientNetworkAdapter;
import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.network.snapshots.OfferTileSnapshot;
import it.polimi.ingsw.network.snapshots.PlayerSnapshot;
import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.cards.buildingCards.RoundFlowBC;
import it.polimi.ingsw.server.model.enums.GamePhase;
import it.polimi.ingsw.server.model.enums.OfferEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import it.polimi.ingsw.network.CommunicationProtocol;

class ClientControllerTest {

    // ==========================================
    // FAKE CLASSES (TEST DOUBLES)
    // ==========================================

    /**
     * Fake UIHandler to capture the interactions requested by the ClientController.
     */
    private static class FakeUIHandler implements UIHandler {
        final List<LobbyUpdateMessage> lobbyUpdates = new ArrayList<>();
        final List<GameStateUpdateMessage> gameUpdates = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final List<GamePhase> prompts = new ArrayList<>();
        final List<String> waitingMessages = new ArrayList<>();
        final List<String> roundFlowWaitingMessages = new ArrayList<>();
        int noCardsPickableCount = 0;
        int roundFlowRequests = 0;
        int shutdowns = 0;
        String lastGameId = null;

        @Override public void onLobbyUpdate(LobbyUpdateMessage update) { lobbyUpdates.add(update); }
        @Override public void onGameStateUpdate(GameStateUpdateMessage update) { gameUpdates.add(update); }
        @Override public void promptForAction(GamePhase phase) { prompts.add(phase); }
        @Override public void displayWaiting(String player) { waitingMessages.add(player); }
        @Override public void displayRoundFlowWaiting(String player) { roundFlowWaitingMessages.add(player); }
        @Override public void displayNoCardsPickable() { noCardsPickableCount++; }
        @Override public void onError(String errorMessage, GamePhase phase) { errors.add(errorMessage); }
        @Override public void onRankingUpdate(RankingUpdateMessage rankingMessage) {}
        @Override public void onRoundFlowCardRequest() { roundFlowRequests++; }
        @Override public void onShutdown() { shutdowns++; }
        @Override public void setGameID(String gameID) { lastGameId = gameID; }
    }

    private static class FakeClientNetworkAdapter implements ClientNetworkAdapter {
        int placeTotemCalls = 0;
        int offerTileCalls = 0;
        int roundFlowCalls = 0;

        int joinLobbyCalls = 0;
        int disconnectCalls = 0;
        String joinedGameId = null;
        boolean forceDisconnectError = false;

        // CountDownLatch safely handles waiting for async threads without busy-waiting loops
        CountDownLatch skipTurnLatch = new CountDownLatch(1);

        @Override public void connect(String host) {}
        @Override public void createLobby(String nickname, int numPlayers) {}

        @Override
        public void joinLobby(String nickname, String gameID) {
            joinLobbyCalls++;
            joinedGameId = gameID;
        }

        @Override
        public void disconnect() throws Exception {
            if (forceDisconnectError) {
                throw new Exception("Simulated disconnect failure");
            }
            disconnectCalls++;
        }

        @Override public void placeTotemOnOfferTile(int tilePosition) { placeTotemCalls++; }
        @Override public void offerTileAction(String cards) { offerTileCalls++; }
        @Override public void roundFlowCardRequest(String cards) { roundFlowCalls++; }

        @Override public void skipTurn() {
            skipTurnLatch.countDown();
        }

        public boolean waitForSkipTurn() throws InterruptedException {
            return skipTurnLatch.await(2, TimeUnit.SECONDS);
        }
    }

    // ==========================================
    // TEST SETUP
    // ==========================================

    private ClientController clientController;
    private FakeClientNetworkAdapter fakeAdapter;
    private FakeUIHandler fakeUI;

    private final String TEST_NICKNAME = "TestPlayer";

    @BeforeEach
    void setUp() throws Exception {
        fakeUI = new FakeUIHandler();
        fakeAdapter = new FakeClientNetworkAdapter();
        clientController = new ClientController(TEST_NICKNAME, fakeUI);

        // Inject the Fake Network Adapter into the controller using Reflection
        Field field = ClientController.class.getDeclaredField("networkAdapter");
        field.setAccessible(true);
        field.set(clientController, fakeAdapter);
    }
// ==========================================
    // SETUP AND CONNECTION TESTS
    // ==========================================

    @Test
    void testSetGameID() {
        clientController.setGameID("LOBBY_123");
        assertEquals("LOBBY_123", fakeUI.lastGameId);
    }

    @Test
    void testJoinLobby_Success() throws Exception {
        // The fake networkAdapter is already injected in the setUp() method
        clientController.joinLobby("Game-123");

        assertEquals(1, fakeAdapter.joinLobbyCalls);
        assertEquals("Game-123", fakeAdapter.joinedGameId);
    }

    @Test
    void testJoinLobby_NullAdapter() throws Exception {
        // Force the networkAdapter to null to cover the if-statement branch
        setField(clientController, "networkAdapter", null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientController.joinLobby("Game-123");
        });
        assertEquals("Client not connected", exception.getMessage());
    }

    @Test
    void testDisconnect_Success() {
        // Calls disconnect normally
        clientController.disconnect();
        assertEquals(1, fakeAdapter.disconnectCalls);
    }

    @Test
    void testDisconnect_NullAdapter() throws Exception {
        // Force adapter to null, it should return immediately without throwing exceptions
        setField(clientController, "networkAdapter", null);

        assertDoesNotThrow(() -> clientController.disconnect());
        assertEquals(0, fakeAdapter.disconnectCalls);
    }

    @Test
    void testDisconnect_ExceptionCaught() {
        // Instruct the fake adapter to throw an exception
        fakeAdapter.forceDisconnectError = true;

        // The controller should catch the exception and print to System.err.
        // We assert that the program does not crash (no unhandled exceptions are thrown)
        assertDoesNotThrow(() -> clientController.disconnect());
        assertEquals(0, fakeAdapter.disconnectCalls); // The actual disconnect was not reached
    }

    @Test
    void testCreateLobbyAndConnect() {
        // The Factory inside the method creates a real adapter (e.g., Socket) which tries
        // to connect to a non-existent server ("localhost" or a closed port).
        // The execution will end with an expected network exception, validating the method flow.
        assertThrows(Exception.class, () -> {
            clientController.createLobbyAndConnect("localhost", 4, CommunicationProtocol.SOCKET);
        });
    }

    @Test
    void testJoinLobbyAndConnect() {
        // Similar to the previous test: tries to actually connect and then calls joinLobby.
        // It covers the first lines of the method until it crashes due to the missing server.
        assertThrows(Exception.class, () -> {
            clientController.joinLobbyAndConnect("localhost", "Game-123", CommunicationProtocol.RMI);
        });
    }
    // ==========================================
    // CALLBACK RECEPTION TESTS
    // ==========================================

    @Test
    void testOnLobbyUpdate() throws Exception {
        LobbyUpdateMessage dummyMessage = instantiateDTO(LobbyUpdateMessage.class);
        clientController.onLobbyUpdate(dummyMessage);
        assertEquals(1, fakeUI.lobbyUpdates.size());
    }

    @Test
    void testOnError() {
        clientController.onError("Invalid move!", GamePhase.PLAYER_TURN);
        assertEquals(1, fakeUI.errors.size());
        assertEquals("Invalid move!", fakeUI.errors.getFirst());
    }

    @Test
    void testOnShutdown() {
        clientController.onShutdown();
        assertEquals(1, fakeUI.shutdowns);
    }

    // ==========================================
    // TURN LOGIC AND UPDATE TESTS
    // ==========================================

    @Test
    void testOnUpdate_NotMyTurn_InteractivePhase() throws Exception {
        GameStateUpdateMessage update = createGameState("AnotherPlayer", GamePhase.PLAYER_TURN, null, null, null, null);

        clientController.onUpdate(update);

        assertEquals(1, fakeUI.gameUpdates.size());
        assertEquals(1, fakeUI.waitingMessages.size());
        assertEquals("AnotherPlayer", fakeUI.waitingMessages.getFirst());
        assertEquals(0, fakeUI.prompts.size()); // Should not prompt the user
    }

    @Test
    void testOnUpdate_NotMyTurn_RoundFlowPending() throws Exception {
        PlayerSnapshot pSnap = instantiateDTO(PlayerSnapshot.class);
        setField(pSnap, "nickname", "AnotherPlayer");
        RoundFlowBC mockBC = instantiateDTO(RoundFlowBC.class);
        setField(pSnap, "ownedBuildings", List.of(mockBC));

        GameStateUpdateMessage update = createGameState("AnotherPlayer", GamePhase.END_ROUND, null, List.of(pSnap), null, null);

        clientController.onUpdate(update);

        assertEquals(1, fakeUI.gameUpdates.size());
        assertEquals(1, fakeUI.roundFlowWaitingMessages.size());
        assertEquals("AnotherPlayer", fakeUI.roundFlowWaitingMessages.getFirst());
    }

    @Test
    void testOnUpdate_MyTurn_PlaceTotems() throws Exception {
        GameStateUpdateMessage update = createGameState(TEST_NICKNAME, GamePhase.PLACE_TOTEMS, null, null, null, null);

        clientController.onUpdate(update);

        assertEquals(1, fakeUI.gameUpdates.size());
        assertEquals(1, fakeUI.prompts.size());
        assertEquals(GamePhase.PLACE_TOTEMS, fakeUI.prompts.getFirst());
    }

    @Test
    void testOnUpdate_MyTurn_PlayerTurn_HasPickableCards() throws Exception {
        OfferTileSnapshot tile = instantiateDTO(OfferTileSnapshot.class);
        setField(tile, "occupantNickname", TEST_NICKNAME);
        setField(tile, "offerEffect", OfferEffect.FOOD); // Food is always pickable

        PlayerSnapshot pSnap = instantiateDTO(PlayerSnapshot.class);
        setField(pSnap, "nickname", TEST_NICKNAME);

        // Passed empty list to topRow to avoid "always null" warning
        GameStateUpdateMessage update = createGameState(TEST_NICKNAME, GamePhase.PLAYER_TURN, List.of(tile), List.of(pSnap), null, new ArrayList<>());

        clientController.onUpdate(update);

        assertEquals(1, fakeUI.prompts.size());
        assertEquals(GamePhase.PLAYER_TURN, fakeUI.prompts.getFirst());
    }

    @Test
    void testOnUpdate_MyTurn_PlayerTurn_NoPickable_AutoSkip() throws Exception {
        OfferTileSnapshot tile = instantiateDTO(OfferTileSnapshot.class);
        setField(tile, "occupantNickname", TEST_NICKNAME);
        setField(tile, "offerEffect", OfferEffect.D); // Requires bottom row calculation

        PlayerSnapshot pSnap = instantiateDTO(PlayerSnapshot.class);
        setField(pSnap, "nickname", TEST_NICKNAME);
        setField(pSnap, "food", 0);
        setField(pSnap, "discountOnBuilding", 0);

        // Expensive building: costs 10 food, player has 0
        BuildingCard expensive = instantiateDTO(RoundFlowBC.class);
        setField(expensive, "foodCost", 10);

        GameStateUpdateMessage update = createGameState(TEST_NICKNAME, GamePhase.PLAYER_TURN, List.of(tile), List.of(pSnap), List.of(expensive), null);

        clientController.onUpdate(update);

        assertEquals(1, fakeUI.noCardsPickableCount);
        assertEquals(0, fakeUI.prompts.size()); // User should not be prompted

        // Wait for the async skipTurn execution (spawned on a new Thread in the controller)
        boolean skipCalled = fakeAdapter.waitForSkipTurn();
        assertTrue(skipCalled, "skipTurn() was not called asynchronously within the timeout");
    }
    @Test
    void testOnRoundFlowCardRequest_NullState() {
        // Se non c'è ancora uno stato salvato (lastGameStateUpdate è null),
        // il controller deve semplicemente chiedere l'input alla UI senza filtri.
        clientController.onRoundFlowCardRequest();

        assertEquals(1, fakeUI.roundFlowRequests);
    }

    @Test
    void testOnRoundFlowCardRequest_NoPickable_AutoSkip() throws Exception {
        // 1. Salviamo uno stato in cui il giocatore NON ha risorse
        PlayerSnapshot pSnap = instantiateDTO(PlayerSnapshot.class);
        setField(pSnap, "nickname", TEST_NICKNAME);
        setField(pSnap, "food", 0);
        setField(pSnap, "discountOnBuilding", 0);

        // Creiamo una carta troppo costosa per simulare che non possa prendere nulla dalla Top Row
        RoundFlowBC expensiveCard = instantiateDTO(RoundFlowBC.class);
        setField(expensiveCard, "foodCost", 10);

        // Passiamo 'expensiveCard' nella topRow (che viene controllata dall'OfferEffect.U)
        GameStateUpdateMessage update = createGameState("AnotherPlayer", GamePhase.END_ROUND,
                new ArrayList<>(), List.of(pSnap), new ArrayList<>(), List.of(expensiveCard));

        clientController.onUpdate(update);

        // 2. Scateniamo la richiesta
        clientController.onRoundFlowCardRequest();

        // 3. Verifichiamo che il client notifichi l'assenza di scelte
        assertEquals(1, fakeUI.noCardsPickableCount);
        assertEquals(0, fakeUI.roundFlowRequests); // Non deve chiedere l'input all'utente

        // 4. Verifichiamo lo skip automatico nel thread asincrono
        boolean skipCalled = fakeAdapter.waitForSkipTurn();
        assertTrue(skipCalled, "skipTurn() non è stato chiamato nel thread asincrono");
    }

    @Test
    void testOnRoundFlowCardRequest_HasPickable_PromptsUser() throws Exception {
        // 1. Salviamo uno stato in cui il giocatore HA carte prendibili
        PlayerSnapshot pSnap = instantiateDTO(PlayerSnapshot.class);
        setField(pSnap, "nickname", TEST_NICKNAME);
        setField(pSnap, "food", 2);
        setField(pSnap, "discountOnBuilding", 0);

        // Creiamo una carta che costa poco (1 cibo) per la Top Row
        RoundFlowBC cheapCard = instantiateDTO(RoundFlowBC.class);
        setField(cheapCard, "foodCost", 1);

        GameStateUpdateMessage update = createGameState("AnotherPlayer", GamePhase.END_ROUND,
                new ArrayList<>(), List.of(pSnap), new ArrayList<>(), List.of(cheapCard));

        clientController.onUpdate(update);

        // 2. Scateniamo la richiesta
        clientController.onRoundFlowCardRequest();

        // 3. Verifichiamo che l'utente venga interrogato correttamente
        assertEquals(1, fakeUI.roundFlowRequests);
        assertEquals(0, fakeUI.noCardsPickableCount); // Non deve bloccare il turno
    }

    // ==========================================
    // REFLECTION UTILITIES
    // ==========================================

    /**
     * Safely instantiates a DTO class by dynamically calling its first constructor.
     * It handles primitives, enums, and injects empty collections to prevent NPEs.
     */
    @SuppressWarnings("unchecked")
    private <T> T instantiateDTO(Class<T> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];

        for (int i = 0; i < args.length; i++) {
            Class<?> pType = constructor.getParameterTypes()[i];
            if (pType == boolean.class) {
                args[i] = false;
            } else if (pType == int.class) {
                args[i] = 0;
            } else if (pType == double.class) {
                args[i] = 0.0;
            } else if (pType.isEnum()) {
                // Previene NPE per i campi Enum (come Era, GamePhase, ecc.)
                args[i] = pType.getEnumConstants()[0];
            } else if (java.util.List.class.isAssignableFrom(pType) || java.util.Collection.class.isAssignableFrom(pType)) {
                args[i] = new ArrayList<>();
            } else if (java.util.Set.class.isAssignableFrom(pType)) {
                args[i] = new java.util.HashSet<>();
            } else if (java.util.Map.class.isAssignableFrom(pType)) {
                args[i] = new java.util.HashMap<>();
            } else if (pType == String.class) {
                args[i] = "";
            } else {
                args[i] = null;
            }
        }
        return (T) constructor.newInstance(args);
    }

    /**
     * Traverses the class hierarchy to forcibly inject a value into a private field.
     */
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Class<?> current = obj.getClass();
        while (current != null) {
            try {
                Field f = current.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(obj, value);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class "
                + obj.getClass().getSimpleName() + " or its superclasses. Check the exact field name!");
    }

    /**
     * Helper to quickly build a GameStateUpdateMessage.
     */
    private GameStateUpdateMessage createGameState(String currentPlayer, GamePhase phase,
                                                   List<OfferTileSnapshot> track,
                                                   List<PlayerSnapshot> players,
                                                   List<Card> bottomRow,
                                                   List<Card> topRow) throws Exception {
        GameStateUpdateMessage msg = instantiateDTO(GameStateUpdateMessage.class);
        setField(msg, "currentPlayerNickname", currentPlayer);
        setField(msg, "currentPhase", phase);
        setField(msg, "offerTrack", track != null ? track : new ArrayList<>());
        setField(msg, "players", players != null ? players : new ArrayList<>());
        setField(msg, "bottomRow", bottomRow != null ? bottomRow : new ArrayList<>());
        setField(msg, "topRow", topRow != null ? topRow : new ArrayList<>());
        return msg;
    }
}