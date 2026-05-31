package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.ServerLogger;

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


/**
 * Handles a single socket client connection on the server side.
 * It parses incoming JSON commands and routes them to SocketServerNetworkAdapter.
 */
public class ClientSocketHandler implements Runnable {

    // Ping-Pongs signals and timeouts
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong lastPongAt = new AtomicLong(System.currentTimeMillis());
    private static final long PING_INTERVAL_MS = 5_000;
    private static final long PONG_TIMEOUT_MS = 15_000;


    private final Socket socket;
    private final SocketServerNetworkAdapter socketServerNetworkAdapter;
    private final Map<String, ClientSocketHandler> connectedClients;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private String gameID;
    private final ObjectMapper mapper = new ObjectMapper();
    // Guard to avoid double-close and duplicate disconnect handling.
    private boolean closed = false;
    private final AtomicBoolean disconnectNotified = new AtomicBoolean(false);

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


    /**
     * Creates a handler bound to a socket connection.
     *
     * @param socket                      the client socket
     * @param socketServerNetworkAdapter  the server adapter to forward actions to
     * @param connectedClients            shared registry of connected clients
     */
    public ClientSocketHandler(Socket socket, SocketServerNetworkAdapter socketServerNetworkAdapter,
                               Map<String, ClientSocketHandler> connectedClients) {
        this.socket = socket;
        this.socketServerNetworkAdapter = socketServerNetworkAdapter;
        this.connectedClients = connectedClients;
    }

    /**
     * Reads client commands and dispatches them until the connection closes.
     */
    @Override
    public void run() {

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            startHeartbeat();

            String line;
            while ((line = in.readLine()) != null) {
                processClientCommand(line);
            }
            notifyDisconnect("Client closed connection");

        } catch (IOException e) {
            System.err.println("[Server] Error handling client: " + e.getMessage());
            notifyDisconnect("Socket error: " + e.getMessage());
        } finally {
            close();
        }
    }


    /**
     * Removes the client from connectedClients map and close the socket
     */
    public void close() {

        if (closed) {
            return;
        }

        closed = true;
        connectedClients.remove(nickname);
        heartbeatScheduler.shutdownNow();

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[Server] Error closing client socket: " + e.getMessage());
        }
    }

    /**
     * Sends a raw JSON message to the client.
     *
     * @param msg the serialized message
     */
    public void send(String msg) {
        out.println(msg);
    }

    public String getGameID() {
        return gameID;
    }



    /**
     * Parses a client command and routes it to the server adapter.
     * Actions supported: register, placeTotem, offerTileAction,
     * roundFlowCardRequest, skipTurn.
     *
     * @param command the raw JSON command string
     */
    private void processClientCommand(String command) {
        try {
            Map<String, Object> cmd = mapper.readValue(command, Map.class);
            String action = (String) cmd.get("action");

            String cards;

            switch (action) {
                case "create_lobby":
                    this.nickname = (String) cmd.get("nickname");
                    int numPlayers = (int) cmd.get("numPlayers");

                    if (isBlank(nickname)) {
                        sendLobbyError("Nickname cannot be empty");
                        break;
                    }

                    // Prevent a new connection with a duplicate nickname from overwriting or disconnecting an active player.
                    ClientSocketHandler existingCreate = connectedClients.putIfAbsent(nickname, this);
                    if (existingCreate != null && existingCreate != this) {
                        sendLobbyError("Nickname already used");
                        break;
                    }
                    boolean addedCreate = existingCreate == null;

                    try {
                        this.gameID = socketServerNetworkAdapter.createGame(nickname, numPlayers);
                        sendGameID();
                    } catch (Exception e) {
                        if (addedCreate) {
                            connectedClients.remove(nickname, this);
                        }
                        sendLobbyError(e.getMessage());
                    }
                    break;

                case "join_lobby":
                    this.nickname = (String) cmd.get("nickname");
                    this.gameID = (String) cmd.get("gameID");

                    if (isBlank(nickname)) {
                        sendLobbyError("Nickname cannot be empty");
                        break;
                    }
                    if (isBlank(gameID)) {
                        sendLobbyError("Game code cannot be empty");
                        break;
                    }

                    // Avoid clobbering an active client's handler when a duplicate nickname is attempted.
                    ClientSocketHandler existingJoin = connectedClients.putIfAbsent(nickname, this);
                    if (existingJoin != null && existingJoin != this) {
                        sendLobbyError("Nickname already used");
                        break;
                    }
                    boolean addedJoin = existingJoin == null;

                    try {
                        socketServerNetworkAdapter.joinGame(nickname, gameID);
                    } catch (Exception e) {
                        if (addedJoin) {
                            connectedClients.remove(nickname, this);
                        }
                        sendLobbyError(e.getMessage());
                    }
                    break;

                case "placeTotem":
                    int tilePosition = (int) cmd.get("tilePosition");
                    socketServerNetworkAdapter.placeTotemOnOfferTile(this.nickname, tilePosition);
                    break;

                case "offerTileAction":
                    cards = (String) cmd.get("cards");
                    socketServerNetworkAdapter.offerTileAction(this.nickname, cards);
                    break;

                case "roundFlowCardRequest":
                    cards = (String) cmd.get("cards");
                    socketServerNetworkAdapter.roundFlowCardRequest(this.nickname, cards);
                    break;

                case "skipTurn":
                    socketServerNetworkAdapter.skipTurn(this.nickname);
                    break;

                case "pong":
                    lastPongAt.set(System.currentTimeMillis());
                    break;
            }
        } catch (IOException e) {
            System.err.println("[Server] Error processing command: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendLobbyError(String message) {
        try {
            String safeMessage = message != null ? message : "Unknown error";
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ERROR");
            msg.put("message", safeMessage);
            msg.put("phase", "LOBBY");
            out.println(mapper.writeValueAsString(msg));
        } catch (IOException e) {
            System.err.println("[Server] Failed to send lobby error: " + e.getMessage());
        }
    }

    private void sendGameID() {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "GAME_ID");
            msg.put("gameID", gameID);
            out.println(mapper.writeValueAsString(msg));
        } catch (IOException e) {
            System.err.println("[Server] Failed to send gameID: " + e.getMessage());
        }
    }

    /**
     * Check if the pong expires, if so calls the methods to close the connections
     */
    private void startHeartbeat() {
        // Start a schedule at a fixed time and check for pong, then send ping
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (now - lastPongAt.get() > PONG_TIMEOUT_MS) {
                notifyDisconnect("Timeout: connection lost with some players");
                close();
                return;
            }
            sendPing();
        }, 0, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }


    /**
     * Send a ping message to the client associated to the socket
     */
    private void sendPing() {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "PING");
            out.println(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            ServerLogger.server("Failed to ping client");
        }
    }

    private void notifyDisconnect(String reason) {
        if (isBlank(nickname)) {
            return;
        }
        if (disconnectNotified.compareAndSet(false, true)) {
            socketServerNetworkAdapter.handleClientDisconnect(nickname, reason);
        }
    }
}
