package org.example.network;

import org.example.network.messages.*;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;
import org.example.server.*;
import org.example.server.model.enums.GamePhase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class HybridServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    private final SocketServerNetworkAdapter socketAdapter;
    private final RMIServerNetworkAdapter rmiAdapter;
    private final Map<String, ServerNetworkAdapter> routingTable = new ConcurrentHashMap<>();

    public HybridServerNetworkAdapter() throws Exception {
        LobbyController sharedLobby = new LobbyController(this, this);

        this.socketAdapter = new SocketServerNetworkAdapter(sharedLobby);
        this.rmiAdapter = new RMIServerNetworkAdapter(sharedLobby);

        socketAdapter.setHybrid(this);
        rmiAdapter.setHybrid(this);
    }

    @Override
    public void start() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
            try {
                socketAdapter.start();
                System.out.println("[HYBRID] Socket started on port " + SocketServerNetworkAdapter.DEFAULT_PORT);
            } catch (Exception e) {
                System.err.println("[HYBRID] Socket start FAILED: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                rmiAdapter.start();
                System.out.println("[HYBRID] RMI started on port " + RMIServerNetworkAdapter.DEFAULT_PORT);
            } catch (Exception e) {
                System.err.println("[HYBRID] RMI start FAILED: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();

        latch.await(); // blocca finché entrambi non sono pronti
        System.out.println("[HYBRID] Both adapters ready.");
    }

    @Override
    public void stop() throws Exception {
        socketAdapter.stop();
        rmiAdapter.stop();
    }

    @Override
    public void onLobbyReady(ServerController serverController) {
        socketAdapter.onLobbyReady(serverController);
        rmiAdapter.onLobbyReady(serverController);
        System.out.println("[HYBRID] Game started with mixed connections.");
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
    public void sendShutdown(String nickname) throws Exception {
        route(nickname).sendShutdown(nickname);
    }

    private ServerNetworkAdapter route(String nickname) {
        ServerNetworkAdapter adapter = routingTable.get(nickname);
        if (adapter == null) throw new IllegalStateException("No route for: " + nickname);
        return adapter;
    }
}