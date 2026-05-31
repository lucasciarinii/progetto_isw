package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;
import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNetworkAdapter;
import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.*;
import org.example.server.model.enums.GamePhase;

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
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Long> lastPongAt = new ConcurrentHashMap<>();
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

    @Override
    public void disconnect(String nickname) throws RemoteException {
        handleClientDisconnect(nickname, "Client requested disconnect");
    }

    @Override
    public void handleClientDisconnect(String nickname, String reason) {
        if (nickname == null || nickname.isBlank()) {
            return;
        }

        connections.remove(nickname);
        if (hybrid != null) {
            hybrid.handleClientDisconnect(nickname, reason);
        }

    }

    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (String nick : connections.keySet()) {
                try {
                    if (now - lastPongAt.getOrDefault(nick, now) > PONG_TIMEOUT_MS) {
                        handleClientDisconnect(nick, "Timeout: connection lost with some players");
                        continue;
                    }
                    // Try to call the method receivePing exposed by RMIClientCallback on client (to verify if it is alive)
                    connections.get(nick).getCallback().receivePing();
                } catch (Exception e) {
                    handleClientDisconnect(nick, "Ping failed: " + e.getMessage());
                }
            }
        }, 0, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void pong(String nickname) throws RemoteException {
        lastPongAt.put(nickname, System.currentTimeMillis());
    }
}
