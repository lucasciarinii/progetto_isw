package org.example.network;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;
import org.example.server.*;
import org.example.server.model.enums.GamePhase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class HybridServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    private SocketServerNetworkAdapter socketAdapter;
    private RMIServerNetworkAdapter rmiAdapter;

    private final Map<String, ServerNetworkAdapter> routingTable = new ConcurrentHashMap<>();
    private final Map<String, String> playerToGameID = new ConcurrentHashMap<>();
    private final Map<String, ServerController> gameControllers = new ConcurrentHashMap<>();

    public HybridServerNetworkAdapter() throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        //LobbyController sharedLobby = new LobbyController(this, this);
    }

    @Override
    public void start() throws Exception {

        MatchManager matchManager = new MatchManager(this, this, this);

        socketAdapter = new SocketServerNetworkAdapter(matchManager, this);
        rmiAdapter = new RMIServerNetworkAdapter(matchManager, this);

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

    @Override
    public void stop() throws Exception {
        socketAdapter.stop();
        rmiAdapter.stop();
    }

    @Override
    public void onLobbyReady(ServerController serverController, String gameID) {
        gameControllers.put(gameID, serverController);
    }

    public void registerRoute(String nickname, ServerNetworkAdapter adapter) {
        routingTable.put(nickname, adapter);
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        route(nickname).sendLobbyUpdate(nickname, update);
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        route(nickname).sendGameStateUpdate(nickname, update);
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        route(nickname).sendError(nickname, errorMessage, phase);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        route(nickname).sendRankingUpdate(nickname, update);
    }

    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        route(nickname).sendRoundFlowCardRequest(nickname);
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        route(nickname).sendShutdown(nickname);
    }


    //! UTILITY METHODS
    public void registerPlayerGameID(String nickname, String gameID) {
        String cleanID = gameID.trim().toUpperCase();
        playerToGameID.put(cleanID, nickname);
    }

    public ServerController resolveServerController(String gameID) {
        String cleanID = gameID.trim().toUpperCase();

        ServerController serverController = gameControllers.get(cleanID);
        if ( serverController == null ) {
            throw new IllegalStateException("No game for: " + cleanID);
        }

        return serverController;
    }

    private ServerNetworkAdapter route(String nickname) {
        ServerNetworkAdapter adapter = routingTable.get(nickname);
        if (adapter == null) throw new IllegalStateException("No route for: " + nickname);
        return adapter;
    }
}