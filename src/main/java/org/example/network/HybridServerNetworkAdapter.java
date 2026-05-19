package org.example.network;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;
import org.example.server.ServerLogger;
import org.example.server.model.enums.GamePhase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Hybrid server adapter that runs both Socket and RMI servers in parallel and
 * routes outbound notifications to the correct protocol based on the player.
 */
public class HybridServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    private final SocketServerNetworkAdapter socketAdapter;
    private final RMIServerNetworkAdapter rmiAdapter;
    private final Map<String, ServerNetworkAdapter> routingTable = new ConcurrentHashMap<>();

    /**
     * Creates a hybrid adapter with a shared lobby for both protocols.
     *
     * @throws Exception if the adapters cannot be initialized
     */
    public HybridServerNetworkAdapter() throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        LobbyController sharedLobby = new LobbyController(this, this);

        this.socketAdapter = new SocketServerNetworkAdapter(sharedLobby);
        this.rmiAdapter = new RMIServerNetworkAdapter(sharedLobby);

        socketAdapter.setHybrid(this);
        rmiAdapter.setHybrid(this);
    }

    /**
     * Starts both Socket and RMI adapters on separate threads.
     *
     * @throws Exception if the adapters fail to start
     */
    @Override
    public void start() throws Exception {
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

        latch.await(); // Blocks until both are ready.
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
    public void onLobbyReady(ServerController serverController) {
        socketAdapter.onLobbyReady(serverController);
        rmiAdapter.onLobbyReady(serverController);
    }

    /**
     * Registers the protocol adapter used by a specific player.
     *
     * @param nickname the player nickname
     * @param adapter  the adapter handling that player's connection (RMI or Socket)
     */
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

    /**
     * Resolves the protocol adapter associated with the given player.
     *
     * @param nickname the player nickname
     * @return the corresponding adapter
     */
    private ServerNetworkAdapter route(String nickname) {
        ServerNetworkAdapter adapter = routingTable.get(nickname);
        if (adapter == null) throw new IllegalStateException("No route for: " + nickname);
        return adapter;
    }
}