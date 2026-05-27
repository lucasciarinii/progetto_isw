package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles a single socket client connection on the server side.
 * It parses incoming JSON commands and routes them to SocketServerNetworkAdapter.
 */
public class ClientSocketHandler implements Runnable {

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

            String line;
            while ((line = in.readLine()) != null) {
                processClientCommand(line);
            }

        } catch (IOException e) {
                System.err.println("[Server] Error handling client: " + e.getMessage());
            } finally {
                close();
            }
    }


    /**
     * Closes the handler and removes the client from the registry.
     */
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (nickname != null) {
            connectedClients.remove(nickname);
            // Notify the server so the match/lobby can be aborted.
            socketServerNetworkAdapter.handleClientDisconnect(nickname, "Client disconnected");
        }
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
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
            }
        } catch (IOException e) {
            System.err.println("[Server] Error processing command: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a lobby error back to the client.
     *
     * @param message the error description
     */
    private void sendLobbyError(String message) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ERROR");
            msg.put("message", message);
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
}
