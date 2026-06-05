package org.example.client;

import org.example.client.view.UIHandler;
import org.example.network.ClientNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.cards.buildingCards.RoundFlowBC;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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

    /**
     * Fake NetworkAdapter to capture commands sent to the server.
     */
    private static class FakeClientNetworkAdapter implements ClientNetworkAdapter {
        int placeTotemCalls = 0;
        int offerTileCalls = 0;
        int roundFlowCalls = 0;

        // CountDownLatch safely handles waiting for async threads without busy-waiting loops
        CountDownLatch skipTurnLatch = new CountDownLatch(1);

        @Override public void connect(String host) {}
        @Override public void createLobby(String nickname, int numPlayers) {}
        @Override public void joinLobby(String nickname, String gameID) {}
        @Override public void disconnect() {} // Implemented to fulfill the interface contract

        @Override public void placeTotemOnOfferTile(int tilePosition) { placeTotemCalls++; }
        @Override public void offerTileAction(String cards) { offerTileCalls++; }
        @Override public void roundFlowCardRequest(String cards) { roundFlowCalls++; }

        @Override public void skipTurn() {
            skipTurnLatch.countDown();
        }

        /**
         * Helper to safely wait for the async skipTurn call (max 2 seconds).
         */
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
    // BASIC COMMANDS TESTS
    // ==========================================

    @Test
    void testGetNickname() {
        assertEquals(TEST_NICKNAME, clientController.getNickname());
    }

    @Test
    void testPlaceTotemOnOfferTile() throws Exception {
        clientController.placeTotemOnOfferTile(3);
        assertEquals(1, fakeAdapter.placeTotemCalls);
    }

    @Test
    void testOfferTileAction() throws Exception {
        clientController.offerTileAction("1,2");
        assertEquals(1, fakeAdapter.offerTileCalls);
    }

    @Test
    void testRoundFlowCardRequest() throws Exception {
        clientController.roundFlowCardRequest("2");
        assertEquals(1, fakeAdapter.roundFlowCalls);
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