package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;
import org.example.server.ServerLogger;
import org.example.server.model.enums.GamePhase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Socket-based server adapter that accepts client connections and forwards
 * incoming actions to the server controller, while pushing events back to clients.
 */
public class SocketServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    public static final int DEFAULT_PORT = 9999;

    private final LobbyController lobby;
    private ServerSocket serverSocket;
    private final Map<String, ClientSocketHandler> connectedClients = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private ServerController serverController;
    private boolean running = false;
    private HybridServerNetworkAdapter hybrid = null;


    /**
     * Creates a socket adapter that shares the lobby with the RMI protocol.
     *
     * @param sharedLobby the shared lobby controller
     */
    public SocketServerNetworkAdapter(LobbyController sharedLobby) {
        this.lobby = sharedLobby;
    }

    /**
     * Registers the hybrid adapter used for routing notifications.
     *
     * @param hybrid the hybrid adapter
     */
    public void setHybrid(HybridServerNetworkAdapter hybrid) {
        this.hybrid = hybrid;
    }

    /**
     * Starts the socket server and begins accepting connections.
     *
     * @throws Exception if the server cannot start
     */
    @Override
    public void start() throws Exception {
        serverSocket = new ServerSocket(DEFAULT_PORT);
        running = true;

        ServerLogger.server("Socket server started on port " + DEFAULT_PORT + ". Waiting for clients...");

        // Accept connections in a dedicated thread.
        new Thread(this::acceptConnections).start();
    }

    /**
     * Stops the socket server and closes all client connections.
     *
     * @throws Exception if shutdown fails
     */
    @Override
    public void stop() throws Exception {
        running = false;

        if ( serverSocket != null && !serverSocket.isClosed() ) {
            serverSocket.close();
        }

        // Close all client connections.
        for ( ClientSocketHandler handler : connectedClients.values() ) {
            handler.close();
        }
    }

    /**
     * Sends a lobby update event to a specific client.
     * Event name: LOBBY_UPDATE (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "LOBBY_UPDATE");
            msg.put("data", mapper.convertValue(update, Map.class));
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Sends a game state update event to a specific client.
     * Event name: GAME_STATE_UPDATE (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "GAME_STATE_UPDATE");
            msg.put("data", mapper.convertValue(update, Map.class));
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Sends an error event to a specific client.
     * Event name: ERROR (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ERROR");
            msg.put("message", errorMessage);
            msg.put("phase", phase != null ? phase.name() : null);
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Sends a ranking update event to a specific client.
     * Event name: RANKING_UPDATE (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "RANKING_UPDATE");
            msg.put("data", mapper.convertValue(update, Map.class));
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Sends a round-flow request event to a specific client.
     * Event name: ROUND_FLOW_CARD_REQUEST (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if(handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ROUND_FLOW_CARD_REQUEST");
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Sends a shutdown event to a specific client.
     * Event name: SHUTDOWN (handled by SocketClientNetworkAdapter).
     */
    @Override
    public void sendShutdown(String nickname) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);
        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "SHUTDOWN");
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    /**
     * Registers a player in the lobby after a socket "register" action.
     *
     * @param nickname   the player's nickname
     * @param numPlayers desired total number of players
     */
    public void registerPlayer(String nickname, int numPlayers) {
        if (hybrid != null)
            hybrid.registerRoute(nickname, this);

        try {
            lobby.registerPlayer(nickname, numPlayers);
        } catch (Exception e) {
            try {
                sendError(nickname, e.getMessage(), GamePhase.LOBBY);
            } catch (Exception ex) {
                ServerLogger.error("Failed to send lobby error: " + ex.getMessage());
            }
        }
    }

    /**
     * Checks whether a nickname is already taken in the lobby.
     *
     * @param nickname the nickname to verify
     * @return true if the nickname is already used
     */
    public boolean isNicknameTaken(String nickname) {
        return lobby.isNicknameTaken(nickname);
    }

    /**
     * Forwards a "placeTotem" action from the socket client to the controller.
     */
    public void placeTotemOnOfferTile(String nickname, int tilePosition) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot place totem.");
            return;
        }
        serverController.placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards an "offerTileAction" command from the socket client to the controller.
     */
    public void offerTileAction(String nickname, String cards) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot perform offer tile action.");
            return;
        }
        serverController.offerTileAction(nickname, cards);
    }

    /**
     * Forwards a "roundFlowCardRequest" command from the socket client to the controller.
     */
    public void roundFlowCardRequest(String nickname, String cards) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot perform round flow card request action.");
            return;
        }
        serverController.roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards a "skipTurn" command from the socket client to the controller.
     */
    public void skipTurn(String nickname) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot skip turn.");
            return;
        }
        serverController.skipTurn(nickname);
    }

    /**
     * Receives the server controller when the lobby is ready.
     */
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
    }

    /**
     * Exposes the current server controller, if any.
     *
     * @return the server controller or null if the game has not started
     */
    public ServerController getServerController() {
        return serverController;
    }

    /**
     * Accepts incoming socket connections and spawns a handler per client.
     */
    private void acceptConnections() {
        while (running) {

            try {

                Socket clientSocket = serverSocket.accept();
                ServerLogger.server("New client connected: " + clientSocket.getInetAddress());

                // Client handler
                ClientSocketHandler handler = new ClientSocketHandler(
                        clientSocket,
                        this,
                        connectedClients
                );

                new Thread(handler).start();

            } catch (IOException e) {
                if (running) {
                    ServerLogger.error("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
}
