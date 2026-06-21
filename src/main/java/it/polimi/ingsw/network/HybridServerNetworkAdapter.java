package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.network.rmi.RMIServerNetworkAdapter;
import it.polimi.ingsw.network.socket.SocketServerNetworkAdapter;
import it.polimi.ingsw.server.LobbyReadyListener;
import it.polimi.ingsw.server.MatchManager;
import it.polimi.ingsw.server.ServerController;
import it.polimi.ingsw.server.ServerLogger;
import it.polimi.ingsw.server.*;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Hybrid server adapter that runs both Socket and RMI servers in parallel and
 * routes outbound notifications to the correct protocol based on the player.
 */
public class HybridServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    private SocketServerNetworkAdapter socketAdapter;
    private RMIServerNetworkAdapter rmiAdapter;
    private MatchManager matchManager;
    private final String serverHost;

    private final Map<String, ServerNetworkAdapter> routingTable = new ConcurrentHashMap<>();
    private final Map<String, String> playerToGameID = new ConcurrentHashMap<>();
    private final Map<String, ServerController> gameControllers = new ConcurrentHashMap<>();

    /**
     * Creates a hybrid adapter with a shared lobby for both protocols.
     *
     */
    public HybridServerNetworkAdapter() {
        this(resolveConfiguredHost());
    }

    /**
     * Creates a hybrid adapter with a shared lobby for both protocols.
     *
     */
    public HybridServerNetworkAdapter(String serverHost) {
        this.serverHost = serverHost;

        System.setProperty("java.rmi.server.hostname", serverHost); // for classic parameters
        //System.setProperty("java.rmi.server.hostname", "25.23.2.248"); // for forced address
        System.setProperty("sun.rmi.transport.tcp.connectTimeout", "3000");
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "3000");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "3000");
        //LobbyController sharedLobby = new LobbyController(this, this);
    }

    /**
     * Starts both Socket and RMI adapters on separate threads.
     *
     * @throws Exception if the adapters fail to start
     */
    @Override
    public void start() throws Exception {

        matchManager = new MatchManager(this, this, this);

        socketAdapter = new SocketServerNetworkAdapter(matchManager, this);
        rmiAdapter = new RMIServerNetworkAdapter(matchManager, this, serverHost);

        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
            try {
                socketAdapter.start();
                ServerLogger.server("Socket ready on port " + SocketServerNetworkAdapter.DEFAULT_PORT);
                latch.countDown();
            } catch (Exception e) {
                ServerLogger.error("Socket start FAILED: " + e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                rmiAdapter.start();
                ServerLogger.server("RMI ready on port " + RMIServerNetworkAdapter.DEFAULT_PORT);
                latch.countDown();
            } catch (Exception e) {
                ServerLogger.error("RMI start FAILED: " + e.getMessage());
            }
        }).start();

        latch.await(); // blocks until both are ready
        ServerLogger.server("Hybrid server network adapter started successfully (RMI + Socket).");
    }

    /**
     * Stops both Socket and RMI adapters.
     *
     * @throws Exception if any adapter fails to stop
     */
    @Override
    public void stop() throws Exception {
        socketAdapter.stop();
        rmiAdapter.stop();
    }

    /**
     * Propagates the ready controller to both protocol adapters.
     *
     * @param serverController the controller for the started match
     */
    @Override
    public void onLobbyReady(ServerController serverController, String gameID) {
        String cleanID = gameID.trim().toUpperCase();
        gameControllers.put(cleanID, serverController);
        matchManager.onLobbyReady(cleanID, serverController);
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        try {
            route(nickname).sendLobbyUpdate(nickname, update);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        try {
            route(nickname).sendGameStateUpdate(nickname, update);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        try {
            route(nickname).sendError(nickname, errorMessage, phase);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        try {
            route(nickname).sendRankingUpdate(nickname, update);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        try {
            route(nickname).sendRoundFlowCardRequest(nickname);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        try {
            route(nickname).sendShutdown(nickname);
        } catch (Exception e) {
            handleClientDisconnect(nickname, "Client disconnected");
            throw e;
        }
    }

    /**
     * Handles a client disconnection by removing its routing entries and aborting
     * the associated game session.
     *
     * @param nickname the nickname of the disconnected player
     * @param reason description of the disconnection cause
     */
    @Override
    public void handleClientDisconnect(String nickname, String reason) {
        String gameID = playerToGameID.remove(nickname);
        routingTable.remove(nickname);
        if (gameID == null) {
            return;
        }
        ServerLogger.server("Client disconnected: " + nickname + " (" + reason + ")");
        matchManager.abortGame(gameID, reason, nickname);
        closeConnectionsForGame(gameID);
    }

    /**
     * Registers the protocol adapter used by a specific player.
     *
     * @param nickname the player nickname
     * @param adapter  the adapter handling that player's connection (RMI or Socket)
     */
    public void registerRoute(String nickname, ServerNetworkAdapter adapter) {
        // Keep the first registered route to avoid breaking an active client when a duplicate nickname appears.
        routingTable.putIfAbsent(nickname, adapter);
    }

    /**
     * Removes all routing and game-ID mappings for the player
     * Used during post-game cleanup when the session has already been terminated
     *
     * @param nickname the nickname of the player to remove
     */
    public void removePlayerMapping(String nickname) {
        playerToGameID.remove(nickname);
        routingTable.remove(nickname);
    }

    /**
     * Associates a player nickname with a game session ID.
     * Called when the player successfully joins or creates a lobby.
     *
     * @param nickname the player nickname
     * @param gameID  the game session ID
     */
    public void registerPlayerGameID(String nickname, String gameID) {
        String cleanID = gameID.trim().toUpperCase();
        playerToGameID.put(nickname, cleanID);
    }

    /**
     * Returns the {@link ServerController} managing the game session of the given player.
     *
     * @param nickname the player nickname
     * @return the {@link ServerController} for the player's current game
     * @throws IllegalStateException if no game session is found for the given nickname
     */
    public ServerController resolveServerControllerByNickname(String nickname) {
        String gameID = playerToGameID.get(nickname);
        ServerController controller = gameControllers.get(gameID);
        if (controller == null) {
            throw new IllegalStateException("Game not started for: " + nickname);
        }
        return controller;
    }

    //! UTILITY METHODS

    /**
     * Resolves the protocol-specific adapter registered for the given player.
     *
     * @param nickname the player nickname
     * @return the {@link ServerNetworkAdapter} handling that player's connection
     * @throws IllegalStateException if no route has been registered for the nickname
     */
    private ServerNetworkAdapter route(String nickname) {
        ServerNetworkAdapter adapter = routingTable.get(nickname);
        if (adapter == null) throw new IllegalStateException("No route for: " + nickname);
        return adapter;
    }

    /**
     * Resolves the server host address from system properties or environment variables,
     * falling back to {@code "127.0.0.1"} if neither is set.
     * Checks {@code mesos.server.host} (system property) first,
     * then {@code SERVER_HOST} (environment variable).
     *
     * @return the resolved host address string
     */
    private static String resolveConfiguredHost() {
        String fromProperty = System.getProperty("mesos.server.host");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim();
        }
        String fromEnv = System.getenv("SERVER_HOST");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return "127.0.0.1";
    }

    /**
     * Closes all active connections belonging to a specific game session on both
     * Socket and RMI adapters. Called after a game is aborted
     *
     * @param gameID the game session ID whose connections should be closed
     */
    public void closeConnectionsForGame(String gameID) {
        if (gameID == null || gameID.isBlank()) {
            return;
        }
        if (socketAdapter != null) {
            socketAdapter.closeConnectionsForGame(gameID);
        }
        if (rmiAdapter != null) {
            rmiAdapter.closeConnectionsForGame(gameID);
        }
    }
}