package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.ClientController;
import org.example.network.ClientNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Socket-based client adapter that sends JSON commands and processes server events.
 */
public class SocketClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController clientController;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ObjectMapper mapper = new ObjectMapper();

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
    public void connect(String host, int port, String nickname, int numPlayers) throws Exception {
        this.nickname = nickname;

        // Create socket to connect to server.
        socket = new Socket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send registration command to server (handled by ClientSocketHandler).
        Map<String, Object> registrationCmd = new HashMap<>();
        registrationCmd.put("action", "register");
        registrationCmd.put("nickname", nickname);
        registrationCmd.put("numPlayers", numPlayers);

        out.println(mapper.writeValueAsString(registrationCmd));

        // Start a thread to receive messages from server.
        startListeningThread();

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
            socket.close();
        }
    }


    /**
     * Listens for server events and dispatches them to the client controller.
     */
    private void startListeningThread() {

        new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    processServerMessage(line);
                }

            } catch (IOException e) {
                System.err.println("[Socket Client] Connection closed or error: " + e.getMessage());
            } finally {
                try {
                    disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
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
            Map<String, Object> msg = mapper.readValue(message, Map.class);
            String event = (String) msg.get("event");

            switch (event) {
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
            }
        } catch (IOException e) {
            System.err.println("[Socket Client] Error processing message: " + e.getMessage());
        }
    }
}
