package it.polimi.ingsw.network.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.network.ClientNetworkAdapter;
import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static it.polimi.ingsw.server.model.enums.GamePhase.GAME_ABORTED;

/**
 * Socket-based client adapter that sends JSON commands and processes server events.
 */
public class SocketClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController clientController;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ObjectMapper mapper = new ObjectMapper();

    // Ping-Pongs signals and timeouts
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong lastPingAt = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean timeoutNotified = new AtomicBoolean(false);
    private static final long PING_TIMEOUT_MS = 20_000;
    private static final long PONG_INTERVAL_MS = 5_000;


    private String nickname;

    /**
     * Creates a client adapter bound to a controller.
     *
     * @param controller the client controller to notify on events
     */
    public SocketClientNetworkAdapter(ClientController controller) {
        this.clientController = controller;
    }

    /**
     * Connects to the socket server and sends the initial "register" command.
     */
    @Override
    public void connect(String host) throws Exception {

        // Create socket to connect to server.
        socket = new Socket(host, SOCKET_PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Start a thread to receive messages from server
        startListeningThread();

    }

    /**
     * Sends a request to the server to create a new lobby and register this client
     * as its first player.
     */
    @Override
    public void createLobby(String nickname, int numPlayers) throws Exception {
        this.nickname = nickname;

        // Send registration command to server
        Map<String, Object> registrationCmd = new HashMap<>();
        registrationCmd.put("action", "create_lobby");
        registrationCmd.put("nickname", nickname);
        registrationCmd.put("numPlayers", numPlayers);

        out.println(mapper.writeValueAsString(registrationCmd));
    }

    /**
     * Sends a request to the server to join an existing lobby.
     */
    @Override
    public void joinLobby(String nickname, String gameID) throws Exception {
        this.nickname = nickname;

        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "join_lobby");
        cmd.put("nickname", nickname);
        cmd.put("gameID", gameID);
        out.println(mapper.writeValueAsString(cmd));
    }

    /**
     * Sends a "placeTotem" command to the server.
     */
    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "placeTotem");
        cmd.put("tilePosition", tilePosition);
        out.println(mapper.writeValueAsString(cmd));
    }

    /**
     * Sends an "offerTileAction" command to the server.
     */
    @Override
    public void offerTileAction(String cards) throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "offerTileAction");
        cmd.put("cards", cards);
        out.println(mapper.writeValueAsString(cmd));
    }

    /**
     * Sends a "roundFlowCardRequest" command to the server.
     */
    @Override
    public void roundFlowCardRequest(String cards) throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "roundFlowCardRequest");
        cmd.put("cards", cards);
        out.println(mapper.writeValueAsString(cmd));
    }

    /**
     * Sends a "skipTurn" command to the server.
     */
    @Override
    public void skipTurn() throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "skipTurn");
        cmd.put("nickname", nickname);
        out.println(mapper.writeValueAsString(cmd));
    }

    /**
     * Disconnects the socket from the server.
     */
    @Override
    public void disconnect() throws Exception {
        if ( socket != null && !socket.isClosed() ) {
            heartbeatScheduler.shutdownNow();
            socket.close();
        }
    }


    /**
     * Listens for server events and dispatches them to the client controller.
     */
    private void startListeningThread() {

        new Thread(() -> {

            startClientTimeoutChecker();

            try {
                String line;

                while ((line = in.readLine()) != null) {
                    processServerMessage(line);
                }

            } catch (IOException e) {
                System.err.println("[Socket Client] Connection closed or error: " + e.getMessage());
            } finally {
                try {
                    clientController.onError("Connection timeout: no ping from server. Please close the game.", GAME_ABORTED);
                    disconnect();
                } catch (Exception e) {
                    System.err.println("[Socket Client] Failed to disconnect cleanly: " + e.getMessage());
                }
            }

        }).start();
    }

    /**
     * Processes a server event message and routes it to the client controller as an "onSomething()" call.
     * Events supported: GAME_STATE_UPDATE, LOBBY_UPDATE, ERROR,
     * RANKING_UPDATE, ROUND_FLOW_CARD_REQUEST, SHUTDOWN.
     *
     * @param message the raw JSON event message
     */
    private void processServerMessage(String message) {
        try {
            Map<String, Object> msg = mapper.readValue(message, new TypeReference<>() {}); // deserialize incoming JSON command (parse JSON command into a generic map)

            String event = (String) msg.get("event");

            switch (event) {

                case "GAME_ID":
                    String gameID = (String) msg.get("gameID");
                    clientController.setGameID(gameID);
                    break;
                case "GAME_STATE_UPDATE":
                    GameStateUpdateMessage update = mapper.convertValue(msg.get("data"), GameStateUpdateMessage.class);
                    clientController.onUpdate(update);
                    break;

                case "LOBBY_UPDATE":
                    LobbyUpdateMessage lobbyUpdate = mapper.convertValue(msg.get("data"), LobbyUpdateMessage.class);
                    clientController.onLobbyUpdate(lobbyUpdate);
                    break;

                case "ERROR":
                    String error = (String) msg.get("message");
                    String phaseRaw = (String) msg.get("phase");
                    GamePhase phase = phaseRaw != null ? GamePhase.valueOf(phaseRaw) : null;
                    clientController.onError(error, phase);
                    break;

                case "RANKING_UPDATE":
                    RankingUpdateMessage rankingUpdate = mapper.convertValue(msg.get("data"), RankingUpdateMessage.class);
                    clientController.onRankingUpdate(rankingUpdate);
                    break;

                case "ROUND_FLOW_CARD_REQUEST":
                    clientController.onRoundFlowCardRequest();
                    break;

                case "SHUTDOWN":
                    clientController.onShutdown();
                    break;

                case "PING":
                    lastPingAt.set(System.currentTimeMillis());
                    Map<String, Object> pong = new HashMap<>();
                    pong.put("action", "pong");
                    out.println(mapper.writeValueAsString(pong));
                    break;
            }
        } catch (IOException e) {
            System.err.println("[Socket Client] Error processing message: " + e.getMessage());
        }
    }

    /**
     * Starts a local timer that checks whether heartbeat pings from the server
     * are still being received. If the timeout expires, the connection is reported
     * as aborted and the socket is closed.
     */
    private void startClientTimeoutChecker() {
        // Start a schedule at a fixed time and check for ping, then send pong

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (now - lastPingAt.get() > PING_TIMEOUT_MS && timeoutNotified.compareAndSet(false, true)) {
                clientController.onError("Connection timeout: no ping from server. Please close the game.", GAME_ABORTED);
                try {
                    disconnect();
                } catch (Exception ignored) {
                }
            }
        }, PONG_INTERVAL_MS, PONG_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
}
