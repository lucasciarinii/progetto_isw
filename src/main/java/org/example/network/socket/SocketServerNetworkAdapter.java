package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.*;
import org.example.server.model.enums.GamePhase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket-based server adapter that accepts client connections and forwards
 * incoming actions to the server controller, while pushing events back to clients.
 */
public class SocketServerNetworkAdapter implements ServerNetworkAdapter {

    public static final int DEFAULT_PORT = 9999;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean running = false;

    private final ConcurrentHashMap<String, ClientSocketHandler> connectedClients = new ConcurrentHashMap<>();
    private final HybridServerNetworkAdapter hybrid;
    private ServerSocket serverSocket;
    private final MatchManager matchManager;


    public SocketServerNetworkAdapter(MatchManager matchManager, HybridServerNetworkAdapter hybrid) {
        this.matchManager = matchManager;
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

        // Close all client connections
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

    public String createGame(String nickname, int numPlayers) throws Exception {
        if (hybrid != null) {
            hybrid.registerRoute(nickname, this);
        }
        return matchManager.createLobby(nickname, numPlayers);
    }

    public void joinGame(String nickname, String gameID) throws Exception {
        if (hybrid != null) {
            hybrid.registerRoute(nickname, this);
        }
        matchManager.joinLobby(nickname, gameID);
    }

    /**
     * Forwards a "placeTotem" action from the socket client to the controller.
     */
    public void placeTotemOnOfferTile(String nickname, int tilePosition) {
        hybrid.resolveServerControllerByNickname(nickname)
                .placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards an "offerTileAction" command from the socket client to the controller.
     */
    public void offerTileAction(String nickname, String cards) {
        hybrid.resolveServerControllerByNickname(nickname)
                .offerTileAction(nickname, cards);
    }

    /**
     * Forwards a "roundFlowCardRequest" command from the socket client to the controller.
     */
    public void roundFlowCardRequest(String nickname, String cards) {
        hybrid.resolveServerControllerByNickname(nickname)
                .roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards a "skipTurn" command from the socket client to the controller.
     */
    public void skipTurn(String nickname) {
        hybrid.resolveServerControllerByNickname(nickname)
                .skipTurn(nickname);
    }

    /**
     * Accepts incoming socket connections and spawns a handler per client.
     */
    private void acceptConnections() {
        while (running) {

            try {

                Socket clientSocket = serverSocket.accept();

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
