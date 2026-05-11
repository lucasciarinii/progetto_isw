package org.example.network.rmi;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ClientConnection;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;
import org.example.server.ServerNotifier;
import org.example.server.model.enums.GamePhase;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/*? Concrete Implementation of the GameServer interface -> server actually implements the "contract" of what it can do for clients.
    Receives RMI calls from clients and delegates them to the ServerController, which contains the actual game logic. */
public class RMIGameServerImpl extends UnicastRemoteObject implements RMIGameServer, LobbyReadyListener, ServerNotifier {

    private final LobbyController lobby;
    private ServerController serverController; // null finché la lobby non è piena
    private final Map<String, ClientConnection> connections = new HashMap<>();

    public RMIGameServerImpl() throws RemoteException {
        super();
        this.lobby = new LobbyController(this, this);
    }

    // Called by LobbyController when the lobby is full and the game can start
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
        System.out.println("[SERVER] Lobby full, game started!");
    }

    @Override
    public void register(String nickname, int numPlayers, RMIClientCallback callback)
            throws Exception {
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

    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }

    //! CLIENT CONNECTION: this methods sends messages to client

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

    //! SERVER STARTUP: Start RMI Registry and registers the server ---------------------------------------------------------------------------
    public static void startServer() throws Exception {
        // Forces RMI to use localhost instead of network board IP
        System.setProperty("java.rmi.server.hostname", "localhost");

        RMIGameServerImpl server = new RMIGameServerImpl();

        // Try to create registry, if exists, reuse it
        try {
            LocateRegistry.createRegistry(1099);
        } catch (ExportException e) {
            System.out.println("[SERVER] Registry already exists, re-use it.");
        }

        java.rmi.Naming.rebind("//localhost/GameServer", server);
        System.out.println("[SERVER] RMI ready on 1099 port. Waiting for players...");
    }
}