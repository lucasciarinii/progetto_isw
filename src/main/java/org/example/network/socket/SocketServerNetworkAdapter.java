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

public class SocketServerNetworkAdapter implements ServerNetworkAdapter, LobbyReadyListener {

    public static final int DEFAULT_PORT = 9999;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean running = false;

    private final Map<String, ClientSocketHandler> connectedClients = new HashMap<>();
    private ServerController serverController;
    private final HybridServerNetworkAdapter hybrid;
    private ServerSocket serverSocket;
    private final MatchManager matchManager;


    // Hybrid constructor (RMI + Socket)
    public SocketServerNetworkAdapter(MatchManager matchManager, HybridServerNetworkAdapter hybrid) {
        this.matchManager = matchManager;
        this.hybrid = hybrid;
    }

    // Start the server
    @Override
    public void start() throws Exception {
        serverSocket = new ServerSocket(DEFAULT_PORT);
        running = true;

        ServerLogger.server("Socket server started on port " + DEFAULT_PORT + ". Waiting for clients...");

        // Thread to accept connections
        new Thread(this::acceptConnections).start();
    }

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

    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if(handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ROUND_FLOW_CARD_REQUEST");
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);
        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "SHUTDOWN");
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    @Override
    public void onLobbyReady(ServerController serverController, String gameID) {
        this.serverController = serverController;
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

    public void placeTotemOnOfferTile(String nickname, int tilePosition) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot place totem.");
            return;
        }
        serverController.placeTotemOnOfferTile(nickname, tilePosition);
    }

    public void offerTileAction(String nickname, String cards) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot perform offer tile action.");
            return;
        }
        serverController.offerTileAction(nickname, cards);
    }

    public void roundFlowCardRequest(String nickname, String cards) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot perform round flow card request action.");
            return;
        }
        serverController.roundFlowCardRequest(nickname, cards);
    }

    public void skipTurn(String nickname) {
        if (serverController == null) {
            ServerLogger.server("Game not started yet. Cannot skip turn.");
            return;
        }
        serverController.skipTurn(nickname);
    }


    public ServerController getServerController() {
        return serverController;
    }

    // Accept client connections
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
