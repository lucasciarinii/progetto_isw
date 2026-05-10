package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;


public class ClientSocketHandler implements Runnable {

    private final Socket socket;
    private final SocketServerNetworkAdapter server;
    private final Map<String, ClientSocketHandler> connectedClients;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private final ObjectMapper mapper = new ObjectMapper();
    private SocketClientConnection clientConnection;


    public ClientSocketHandler(Socket socket, SocketServerNetworkAdapter server,
                               Map<String, ClientSocketHandler> connectedClients) {
        this.socket = socket;
        this.server = server;
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

            switch (action) {
                case "register":
                    String nickname = (String) cmd.get("nickname");
                    int numPlayers = (int) cmd.get("numPlayers");
                    this.nickname = nickname;
                    connectedClients.put(nickname, this);

                    if (clientConnection == null) {
                        clientConnection = new SocketClientConnection(out);
                    }
                    server.registerPlayer(nickname, numPlayers, clientConnection);
                    System.out.println("[SERVER] Player " + nickname + " registered");
                    break;

                case "placeTotem":
                    int tilePosition = (int) cmd.get("tilePosition");
                    server.placeTotemOnOfferTile(this.nickname, tilePosition);
                    break;

                case "offerTileAction":
                    String cards = (String) cmd.get("cards");
                    server.offerTileAction(this.nickname, cards);
                    break;

                case "skipTurn":
                    server.skipTurn(this.nickname);
                    break;
            }
        } catch (IOException e) {
            System.err.println("[Server] Error processing command: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
