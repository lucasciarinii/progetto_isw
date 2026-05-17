package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ClientSocketHandler implements Runnable {

    private final Socket socket;
    private final SocketServerNetworkAdapter socketServerNetworkAdapter;
    private final Map<String, ClientSocketHandler> connectedClients;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private final ObjectMapper mapper = new ObjectMapper();


    public ClientSocketHandler(Socket socket, SocketServerNetworkAdapter socketServerNetworkAdapter,
                               Map<String, ClientSocketHandler> connectedClients) {
        this.socket = socket;
        this.socketServerNetworkAdapter = socketServerNetworkAdapter;
        this.connectedClients = connectedClients;
    }

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


    public void close() {
        if (nickname != null) {
            connectedClients.remove(nickname);
        }
    }

    public void send(String msg) {
        out.println(msg);
    }



    private void processClientCommand(String command) {
        try {
            Map<String, Object> cmd = mapper.readValue(command, Map.class);
            String action = (String) cmd.get("action");
            String cards;

            switch (action) {
                case "register":
                    String nickname = (String) cmd.get("nickname");
                    int numPlayers = (int) cmd.get("numPlayers");

                    if (socketServerNetworkAdapter.isNicknameTaken(nickname)) {
                        sendLobbyError("Nickname already used: " + nickname);
                        break;
                    }
                    this.nickname = nickname;
                    connectedClients.put(nickname, this);
                    socketServerNetworkAdapter.registerPlayer(nickname, numPlayers);
                    System.out.println("[SERVER] Player " + nickname + " registered");
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
}
