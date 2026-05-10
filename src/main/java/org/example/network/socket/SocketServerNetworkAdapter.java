package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ServerController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketServerNetworkAdapter implements ServerNetworkAdapter {

    private final ServerController serverController;
    private ServerSocket serverSocket;
    private final Map<String, ClientSocketHandler> connectedClients = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private boolean running = false;

    public SocketServerNetworkAdapter(ServerController serverController) {
        this.serverController = serverController;
    }


    // Start the server
    @Override
    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("[SERVER] Socket server started on port " + port + ". Waiting for clients...");

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
    public void sendError(String nickname, String errorMessage) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);

        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "ERROR");
            msg.put("message", errorMessage);
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
    public void sendShutdown(String nickname) throws Exception {
        ClientSocketHandler handler = connectedClients.get(nickname);
        if (handler != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("event", "SHUTDOWN");
            handler.send(mapper.writeValueAsString(msg));
        }
    }

    // Accept client connections
    private void acceptConnections() {
        while (running) {

            try {

                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Accepted connection from " + clientSocket.getInetAddress());

                // Client handler
                ClientSocketHandler handler = new ClientSocketHandler(
                        clientSocket,
                        this,
                        connectedClients
                );

                new Thread(handler).start();

            } catch (IOException e) {
                if (running) {
                    System.err.println("[SERVER] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public ServerController getServerController() {
        return serverController;
    }
}
