package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.client.rmi.RMIClientCallback;
import it.polimi.ingsw.network.HybridServerNetworkAdapter;
import it.polimi.ingsw.network.ServerNetworkAdapter;
import it.polimi.ingsw.network.ServerNotifier;
import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.server.MatchManager;
import it.polimi.ingsw.server.ServerLogger;
import it.polimi.ingsw.server.*;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RMI-based server adapter that exposes RMIGameServer and dispatches actions
 * to the ServerController while using callbacks to notify clients.
 */
public class  RMIServerNetworkAdapter extends UnicastRemoteObject implements ServerNetworkAdapter, RMIGameServer {

    public static final int DEFAULT_PORT = 1099;
    private final Map<String, RMIClientConnection> connections = new ConcurrentHashMap<>();
    private final String serverHost;

    private final HybridServerNetworkAdapter hybrid;
    private final MatchManager matchManager;

    // Ping-Pongs signals and timeouts
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(20);
    private final ConcurrentHashMap<String, Long> lastPongAt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> playerGameIds = new ConcurrentHashMap<>();
    private static final long PING_INTERVAL_MS = 5_000;
    private static final long PONG_TIMEOUT_MS = 15_000;

    /**
     * Creates an RMI adapter.
     * @param matchManager handling multiple lobbies
     * @param hybrid hybrid adapter
     * @throws RemoteException if the remote object cannot be exported
     */
    public RMIServerNetworkAdapter(MatchManager matchManager, HybridServerNetworkAdapter hybrid, String serverHost) throws RemoteException {
        super();
        this.matchManager = matchManager;
        this.hybrid = hybrid;
        this.serverHost = serverHost;
    }

    /**
     * Starts the RMI registry and binds the RMIGameServer stub.
     */
    @Override
    public void start() throws Exception {

        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);
        } catch (ExportException e) {
            ServerLogger.error("Failed to create RMI registry or it already exists: " + e.getMessage());
        }

        Naming.rebind("//" + serverHost + ":" + DEFAULT_PORT + "/GameServer", this);
        ServerLogger.server("RMI ready on port " + DEFAULT_PORT + ". Waiting for players...");

        startHeartbeat();
    }

    /**
     * RMI does not require an explicit stop in this implementation.
     */
    @SuppressWarnings("RedundantThrows")
    @Override
    public void stop() throws Exception {
        // RMI doesn't explicitly require a stop
    }

    /**
     * Sends a lobby update through the client callback.
     */
    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendLobbyUpdate(nickname, update);
    }

    /**
     * Sends a game state update through the client callback.
     */
    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendGameStateUpdate(nickname, update);
    }

    /**
     * Sends an error through the client callback.
     */
    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendError(nickname, errorMessage, phase);
    }

    /**
     * Sends a ranking update through the client callback.
     */
    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendRankingUpdate(nickname, update);
    }

    /**
     * Sends a RoundFlow request through the client callback.
     */
    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendRoundFlowCardRequest(nickname);
    }

    /**
     * Sends a shutdown signal through the client callback.
     */
    @Override
    public void sendShutdown(String nickname) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendShutdown(nickname);
    }


    /**
     * Create a lobby
     *
     * @param nickname player nickname chosen
     * @param numPlayers number of players for the lobby
     * @param callback callback for the RMIClientConnection
     */
    @Override
    public String createLobby(String nickname, int numPlayers, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        // Prevent overwriting an active client's callback when a duplicate nickname is attempted.
        if (connections.putIfAbsent(nickname, connection) != null) {
            connection.sendError(nickname, "Registration Error: Nickname already used", GamePhase.LOBBY);
            throw new IllegalArgumentException("Nickname already used");
        }
        hybrid.registerRoute(nickname, this);

        try {
            String gameID = matchManager.createLobby(nickname, numPlayers);
            lastPongAt.put(nickname, System.currentTimeMillis()); // set here because from here the client is connected
            if (gameID != null) {
                playerGameIds.put(nickname, gameID.trim().toUpperCase());
            }
            return gameID;
        } catch (Exception e) {
            connections.remove(nickname, connection);
            connection.sendError(nickname, "Registration Error: " + e.getMessage(), GamePhase.LOBBY);
            throw e;
        }
    }

    /**
     * Join an existing lobby
     *
     * @param nickname player nickname chosen
     * @param gameID lobby ID
     * @param callback callback for the RMIClientConnection
     */
    @Override
    public void joinLobby(String nickname, String gameID, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        if (connections.putIfAbsent(nickname, connection) != null) {
            connection.sendError(nickname, "Registration Error: Nickname already used", GamePhase.LOBBY);
            throw new IllegalArgumentException("Nickname already used");
        }
        hybrid.registerRoute(nickname, this);

        try {
            matchManager.joinLobby(nickname, gameID);
            lastPongAt.put(nickname, System.currentTimeMillis()); // set here because from here the client is connected
            if (gameID != null) {
                playerGameIds.put(nickname, gameID.trim().toUpperCase());
            }
        } catch (Exception e) {
            connections.remove(nickname, connection);
            connection.sendError(nickname, "Registration Error: " + e.getMessage(), GamePhase.LOBBY);
            throw e;
        }
    }

    /**
     * Forwards a totem placement to the server controller.
     */
    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards an offer tile action to the server controller.
     */
    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .offerTileAction(nickname, cards);
    }

    /**
     * Forwards a RoundFlow request to the server controller.
     */
    @Override
    public void roundFlowCardRequest(String nickname, String cards) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards a skip turn request to the server controller.
     */
    @Override
    public void skipTurn(String nickname) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .skipTurn(nickname);
    }

    /**
     * Handles an explicit client-side disconnect request.
     *
     * @param nickname the nickname of the disconnecting player
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void disconnect(String nickname) throws RemoteException {
        handleClientDisconnect(nickname, "Client requested disconnect");
    }

    /**
     * Removes the disconnected client from the internal RMI tracking structures (connections map)
     * and delegates the higher-level disconnect handling to the hybrid adapter.
     * If no hybrid adapter is available, it falls back to local cleanup for the
     * associated game session.
     *
     * @param nickname the nickname of the disconnected player
     * @param reason   a human-readable description of the disconnection cause
     */
    @Override
    public void handleClientDisconnect(String nickname, String reason) {
        if (nickname == null || nickname.isBlank()) {
            return;
        }

        String gameID = playerGameIds.remove(nickname);
        connections.remove(nickname);
        lastPongAt.remove(nickname);

        if (hybrid != null) {
            hybrid.handleClientDisconnect(nickname, reason);
        } else if (gameID != null && !gameID.isBlank()) {
            closeConnectionsForGame(gameID);
        }

    }

    /**
     * Removes all RMI-side connection state associated with the specified game session,
     * including callback connections, heartbeat timestamps, and player-to-game mappings.
     *
     * @param gameID the identifier of the game session to clean up
     */
    public void closeConnectionsForGame(String gameID) {
        if (gameID == null || gameID.isBlank()) {
            return;
        }
        for (Map.Entry<String, String> entry : playerGameIds.entrySet()) {
            String nick = entry.getKey();
            String entryGameID = entry.getValue();
            if (entryGameID != null && entryGameID.equalsIgnoreCase(gameID)) {
                playerGameIds.remove(nick, entryGameID);
                connections.remove(nick);
                lastPongAt.remove(nick);
            }
        }
    }

    /**
     * Starts the periodic heartbeat task used to detect unresponsive RMI clients.
     * Each cycle checks the latest pong timestamp for every connected client and
     * sends a ping through the client callback. Clients that exceed the configured
     * timeout or fail to answer are treated as disconnected.
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            // Use a copy to avoid ConcurrentModificationException
            for (String nick : new java.util.ArrayList<>(connections.keySet())) {
                RMIClientConnection conn = connections.get(nick);
                if (conn == null) continue;
                try {
                    // 1. First check the pong timeout
                    long lastPong = lastPongAt.getOrDefault(nick, 0L);
                    if (lastPong > 0 && now - lastPong > PONG_TIMEOUT_MS) {
                        handleClientDisconnect(nick, "Pong timeout: client non risponde");
                        continue;
                    }
                    // 2. Then send a ping: if the client is unresponsive, it will trigger a RemoteException and be handled in the catch block
                    conn.getCallback().receivePing();
                } catch (Exception e) {
                    handleClientDisconnect(nick, "Ping failed: " + e.getMessage());
                }
            }
        }, PING_INTERVAL_MS, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Records the timestamp of the latest pong received from the given client,
     * resetting its inactivity timer in the heartbeat
     *
     * @param nickname the player's nickname
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void pong(String nickname) throws RemoteException {
        lastPongAt.put(nickname, System.currentTimeMillis());
    }
}
