package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;
import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.*;
import org.example.server.model.enums.GamePhase;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RMIServerNetworkAdapter extends UnicastRemoteObject implements ServerNetworkAdapter, RMIGameServer, LobbyReadyListener {

    public static final int DEFAULT_PORT = 1099;
    private final LobbyController lobby;
    private ServerController serverController;
    private final Map<String, ServerNotifier> connections = new HashMap<>();
    private HybridServerNetworkAdapter hybrid = null;


    // Hybrid constructor (RMI + Socket)
    public RMIServerNetworkAdapter(LobbyController sharedLobby) throws RemoteException {
        super();
        this.lobby = sharedLobby;
    }

    public void setHybrid(HybridServerNetworkAdapter hybrid) {
        this.hybrid = hybrid;
    }


    @Override
    public void start() throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");

        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);
        } catch (ExportException e) {
            ServerLogger.error("Failed to create RMI registry or it already exists: " + e.getMessage());
        }

        java.rmi.Naming.rebind("//127.0.0.1:" + DEFAULT_PORT + "/GameServer", this);
        ServerLogger.server("RMI ready on port " + DEFAULT_PORT + ". Waiting for players...");
    }

    @Override
    public void stop() throws Exception {
        // RMI doesn't explicitly require a stop
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendLobbyUpdate(nickname, update);
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendGameStateUpdate(nickname, update);
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendError(nickname, errorMessage, phase);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendRankingUpdate(nickname, update);
    }

    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        ServerNotifier connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendShutdown(nickname);
    }


    // RMIGameServer methods


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

    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        checkGameStarted();
        serverController.placeTotemOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        checkGameStarted();
        serverController.offerTileAction(nickname, cards);
    }

    @Override
    public void skipTurn(String nickname) throws RemoteException {
        checkGameStarted();
        serverController.skipTurn(nickname);
    }

    // LOBBY methods
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
        ServerLogger.server("Lobby full, game started!");
    }


    //UTILITY METHODS
    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }
}
