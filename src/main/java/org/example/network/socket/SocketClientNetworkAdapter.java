package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.client.ClientController;
import org.example.network.ClientNetworkAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController clientController;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ObjectMapper mapper = new ObjectMapper();

    private String nickname;

    public SocketClientNetworkAdapter(ClientController controller) {
        this.clientController = controller;
    }

    @Override
    public void connect(String host, int port, String nickname, int numPlayers) throws Exception {
        this.nickname = nickname;

        // Create socket to connect to server
        socket = new Socket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send registration command to server
        Map<String, Object> registrationCmd = new HashMap<>();
        registrationCmd.put("action", "register");
        registrationCmd.put("nickname", nickname);
        registrationCmd.put("numPlayers", numPlayers);

        out.println(mapper.writeValueAsString(registrationCmd));

        // Start a thread to receive messages from server
        startListeningThread();

    }

    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "placeTotem");
        cmd.put("tilePosition", tilePosition);
        out.println(mapper.writeValueAsString(cmd));
    }

    @Override
    public void offerTileAction(String cards) throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "offerTileAction");
        cmd.put("cards", cards);
        out.println(mapper.writeValueAsString(cmd));
    }

    @Override
    public void skipTurn() throws Exception {
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("action", "skipTurn");
        cmd.put("nickname", nickname);
        out.println(mapper.writeValueAsString(cmd));
    }

    @Override
    public void disconnect() throws Exception {
        if ( socket != null && !socket.isClosed() ) {
            socket.close();
        }
    }


    // this method listen the messages from the server
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

    private void processServerMessage(String message) {
        try {
            Map<String, Object> msg = mapper.readValue(message, Map.class);
            String event = (String) msg.get("event");

            switch (event) {
                case "GAME_STATE_UPDATE":
                    // TODO: deserializza GameStateUpdateMessage e chiama clientController.onUpdate()
                    break;

                case "LOBBY_UPDATE":
                    // TODO: deserializza LobbyUpdateMessage e chiama clientController.onLobbyUpdate()
                    break;

                case "ERROR":
                    String error = (String) msg.get("message");
                    // TODO: chiama clientController.onError()
                    break;

                case "RANKING_UPDATE":
                    // TODO: deserializza RankingUpdateMessage e chiama clientController.onRankingUpdate()
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
