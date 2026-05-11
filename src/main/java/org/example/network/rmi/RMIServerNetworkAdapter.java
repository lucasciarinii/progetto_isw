package org.example.network.rmi;

import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ClientConnection;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;
import org.example.server.model.enums.GamePhase;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RMIServerNetworkAdapter extends UnicastRemoteObject implements ServerNetworkAdapter, RMIGameServer, LobbyReadyListener {

    private final LobbyController lobby;
    private ServerController serverController;
    private final Map<String, ClientConnection> connections = new HashMap<>();

    public RMIServerNetworkAdapter() throws RemoteException {
        super();
        this.lobby = new LobbyController(this, this);
    }


    @Override
    public void start(int port) throws Exception {
        System.setProperty("java.rmi.server.hostname", "localhost");

        try {
            LocateRegistry.createRegistry(port);
        } catch (ExportException e) {
            System.out.println("[SERVER] RMI registry already exists.");
        }

        java.rmi.Naming.rebind("//localhost:" + port + "/GameServer", this);
        System.out.println("[SERVER] RMI ready on port " + port + ". Waiting for players...");
    }

    @Override
    public void stop() throws Exception {
        // RMI doesn't explicitly require a stop
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        ClientConnection connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendLobbyUpdate(update);
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        checkGameStarted();
        ClientConnection connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendGameStateUpdate(update);
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        ClientConnection connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendError(errorMessage, phase);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        checkGameStarted();
        ClientConnection connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendRankingUpdate(update);
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        checkGameStarted();
        ClientConnection connection = connections.get(nickname);
        if (connection == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        connection.sendShutdown();
    }


    // RMIGameServer methods


    @Override
    public void register(String nickname, int numPlayers, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        if (lobby.isNicknameTaken(nickname)) {
            connection.sendError("Nickname already used: " + nickname, GamePhase.LOBBY);
            return;
        }
        connections.put(nickname, connection);
        try {
            lobby.registerPlayer(nickname, numPlayers);
        } catch (Exception e) {
            connections.remove(nickname);
            connection.sendError("Registration Error: " + e.getMessage(), GamePhase.LOBBY);
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
        System.out.println("[SERVER] Lobby full, game started!");
    }


    //UTILITY METHODS
    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }
}
