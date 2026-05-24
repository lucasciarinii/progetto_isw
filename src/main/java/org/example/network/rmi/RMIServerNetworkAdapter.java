package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;
import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNetworkAdapter;
import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;
import org.example.server.ServerLogger;
import org.example.server.model.enums.GamePhase;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * RMI-based server adapter that exposes RMIGameServer and dispatches actions
 * to the ServerController while using callbacks to notify clients.
 */
public class RMIServerNetworkAdapter extends UnicastRemoteObject implements ServerNetworkAdapter, RMIGameServer, LobbyReadyListener {

    public static final int DEFAULT_PORT = 1099;
    private final LobbyController lobby;
    private ServerController serverController;
    private final Map<String, ServerNotifier> connections = new HashMap<>();
    private HybridServerNetworkAdapter hybrid = null;


    /**
     * Creates an RMI adapter with a shared lobby.
     *
     * @param sharedLobby the shared lobby controller
     * @throws RemoteException if the remote object cannot be exported
     */
    public RMIServerNetworkAdapter(LobbyController sharedLobby) throws RemoteException {
        super();
        this.lobby = sharedLobby;
    }

    /**
     * Registers the hybrid adapter used for routing notifications.
     *
     * @param hybrid the hybrid adapter
     */
    public void setHybrid(HybridServerNetworkAdapter hybrid) {
        this.hybrid = hybrid;
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

        java.rmi.Naming.rebind("//127.0.0.1:" + DEFAULT_PORT + "/GameServer", this);
        ServerLogger.server("RMI ready on port " + DEFAULT_PORT + ". Waiting for players...");
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


    // RMIGameServer methods


    /**
     * Registers a client callback and forwards the player to the lobby.
     */
    @Override
    public void register(String nickname, int numPlayers, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        if (lobby.isNicknameTaken(nickname)) {
            connection.sendError(nickname, "Nickname already used: " + nickname, GamePhase.LOBBY);
            return;
        }
        connections.put(nickname, connection);
        if (hybrid != null)
            hybrid.registerRoute(nickname, this);

        try {
            lobby.registerPlayer(nickname, numPlayers);
        } catch (Exception e) {
            connections.remove(nickname);
            connection.sendError(nickname, "Registration Error: " + e.getMessage(), GamePhase.LOBBY);
        }
    }

    /**
     * Forwards a totem placement to the server controller.
     */
    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        checkGameStarted();
        serverController.placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards an offer tile action to the server controller.
     */
    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        checkGameStarted();
        serverController.offerTileAction(nickname, cards);
    }


    /**
     * Forwards a RoundFlow request to the server controller.
     */
    @Override
    public void roundFlowCardRequest(String nickname, String cards) throws RemoteException {
        checkGameStarted();
        serverController.roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards a skip turn request to the server controller.
     */
    @Override
    public void skipTurn(String nickname) throws RemoteException {
        checkGameStarted();
        serverController.skipTurn(nickname);
    }

    // LOBBY methods
    /**
     * Receives the controller instance when the lobby is full.
     */
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
        ServerLogger.server("Lobby full, game started!");
    }


    // Utility methods
    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }
}

