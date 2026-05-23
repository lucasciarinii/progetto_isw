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
import java.util.HashMap;
import java.util.Map;

public class  RMIServerNetworkAdapter extends UnicastRemoteObject implements ServerNetworkAdapter, RMIGameServer {

    public static final int DEFAULT_PORT = 1099;
    private final Map<String, ServerNotifier> connections = new HashMap<>();

    private final HybridServerNetworkAdapter hybrid;
    private final MatchManager matchManager;




    // Hybrid constructor (RMI + Socket)
    public RMIServerNetworkAdapter(MatchManager matchManager, HybridServerNetworkAdapter hybrid) throws RemoteException {
        super();
        this.matchManager = matchManager;
        this.hybrid = hybrid;
    }


    @Override
    public void start() throws Exception {

        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);
        } catch (ExportException e) {
            ServerLogger.error("Failed to create RMI registry or it already exists: " + e.getMessage());
        }

        Naming.rebind("//127.0.0.1:" + DEFAULT_PORT + "/GameServer", this);
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
        connection.sendRoundFlowCardRequest(nickname);
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
    public String createLobby(String nickname, int numPlayers, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        connections.put(nickname, connection);
        hybrid.registerRoute(nickname, this);

        try {
            return matchManager.createLobby(nickname, numPlayers);
        } catch (Exception e) {
            connections.remove(nickname);
            connection.sendError(nickname, "Registration Error: " + e.getMessage(), GamePhase.LOBBY);
            throw e;
        }
    }

    @Override
    public void joinLobby(String nickname, String gameID, RMIClientCallback callback) throws Exception {
        RMIClientConnection connection = new RMIClientConnection(callback);
        connections.put(nickname, connection);
        hybrid.registerRoute(nickname, this);

        try {
            matchManager.joinLobby(nickname, gameID);
        } catch (Exception e) {
            connections.remove(nickname);
            connection.sendError(nickname, "Registration Error: " + e.getMessage(), GamePhase.LOBBY);
            throw e;
        }
    }

    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .placeTotemOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .offerTileAction(nickname, cards);
    }

    @Override
    public void roundFlowCardRequest(String nickname, String cards) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .roundFlowCardRequest(nickname, cards);
    }

    @Override
    public void skipTurn(String nickname) throws RemoteException {
        hybrid.resolveServerControllerByNickname(nickname)
                .skipTurn(nickname);
    }
}
