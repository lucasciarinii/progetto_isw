package org.example.network.rmi;

import org.example.client.ClientController;
import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.network.ClientNetworkAdapter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.server.model.enums.GamePhase.GAME_ABORTED;

/**
 * RMI-based client adapter that invokes RMIGameServer methods on the server.
 */
public class RMIClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController clientController;
    private RMIGameServer server;
    private String nickname;

    private final AtomicLong lastPingAt = new AtomicLong(System.currentTimeMillis());
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean timeoutNotified = new AtomicBoolean(false);
    private static final long PING_TIMEOUT_MS = 20_000;
    private static final long PONG_INTERVAL_MS = 5_000;

    /**
     * Creates a client adapter bound to a controller.
     *
     * @param controller the client controller that receives callbacks
     */
    public RMIClientNetworkAdapter(ClientController controller) {
        this.clientController = controller;
    }

    /**
     * Connects to the RMI registry and registers the client callback.
     * The server will invoke RMIClientCallback methods to push updates.
     */
    @Override
    public void connect(String host) throws Exception {
        System.setProperty("java.rmi.server.hostname", resolveClientHost());
        String url = "rmi://" + host + ":" + RMI_PORT + "/GameServer";
        server = (RMIGameServer) Naming.lookup(url);
    }

    /**
     * Resolves the client host address to be exported for incoming RMI callbacks.
     * The resolution order is: system property, environment variable, first
     * site-local IPv4 address, then localhost as a final fallback.
     *
     * @return the host address that the client exposes for RMI callbacks
     */
    private static String resolveClientHost() {
        String fromProperty = System.getProperty("mesos.client.host");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim();
        }
        String fromEnv = System.getenv("CLIENT_HOST");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to localhost below if detection fails.
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return "127.0.0.1";
        }
    }

    /**
     * Creates a new lobby on the remote server and registers the client callback
     * used to receive asynchronous server notifications.
     */
    @Override
    public void createLobby(String nickname, int numPlayers) throws Exception {
        this.nickname = nickname;
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(clientController, server, nickname, lastPingAt);
        String gameID = server.createLobby(nickname, numPlayers, callback);
        startClientTimeoutChecker();
        clientController.setGameID(gameID);
    }

    /**
     * Joins an existing lobby on the remote server and registers the client callback
     * used to receive asynchronous server notifications.
     */
    @Override
    public void joinLobby(String nickname, String gameID) throws Exception {
        this.nickname = nickname;
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(clientController, server, nickname, lastPingAt);
        server.joinLobby(nickname, gameID, callback);
        startClientTimeoutChecker();
    }

    /**
     * Forwards the action to RMIGameServer.placeTotemOnOfferTile.
     */
    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        server.placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards the action to RMIGameServer.offerTileAction.
     */
    @Override
    public void offerTileAction(String cards) throws Exception {
        server.offerTileAction(nickname, cards);
    }

    /**
     * Forwards the action to RMIGameServer.roundFlowCardRequest.
     */
    @Override
    public void roundFlowCardRequest(String cards) throws Exception {
        server.roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards the action to RMIGameServer.skipTurn.
     */
    @Override
    public void skipTurn() throws Exception {
        server.skipTurn(nickname);
    }

    /**
     * RMI does not require an explicit disconnect for this client.
     */
    @Override
    public void disconnect() throws Exception {
        if (server == null || nickname == null || nickname.isBlank()) {
            return;
        }
        server.disconnect(nickname);
    }


    /**
     * Starts a local timer that monitors the arrival of heartbeat pings from the server.
     * If no ping is received within the configured timeout, the client reports the
     * connection as aborted and attempts a local disconnect.
     */
    private void startClientTimeoutChecker() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (now - lastPingAt.get() > PING_TIMEOUT_MS && timeoutNotified.compareAndSet(false, true)) {
                clientController.onError("Connection timeout: no ping from server. Please close the game.", GAME_ABORTED);
                heartbeatScheduler.shutdownNow();
                try {
                    disconnect();
                } catch (Exception ignored) {
                }
            }
        }, PONG_INTERVAL_MS, PONG_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
}
