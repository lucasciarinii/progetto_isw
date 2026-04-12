package org.example.server;

/*? Handle waiting phase before the game starts.
    - First player decides how many players will be in the game (2-5)
    - When the number is reached, creates Match and ServerController.
*/

import org.example.model.match.Match;
import org.example.model.match.Player;
import org.example.network.LobbyUpdateMessage;
import org.example.server.rmi.ClientCallback;
import org.example.server.rmi.RMIClientConnection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LobbyController {
    private int requiredPlayers = -1;  // -1 = not decided yet

    // Keeps the order of connection: nickname, callback
    private final Map<String, ClientCallback> waitingClients = new LinkedHashMap<>();

    // Callback called by GameServerImpl when the lobby is full
    private final LobbyReadyListener onReady;

    public LobbyController(LobbyReadyListener onReady) {
        this.onReady = onReady;
    }

    /* Registers a new player in the lobby
        - @param nickname       player's nickname
        - @param numPlayers     desired number of players (used only by the first one)
        - @param callback       RMI callback to communicate with this client
    */
    public synchronized void registerPlayer(String nickname, int numPlayers, ClientCallback callback) throws Exception {

        // Check if the nickname is already taken
        if (waitingClients.containsKey(nickname)) {
            callback.receiveError("Nickname already used: " + nickname);
            return;
        }

        // First player decides how many players will be in the game (2-5)
        if (waitingClients.isEmpty()) {
            if (numPlayers < 2 || numPlayers > 5) {
                callback.receiveError("Invalid number of players. Choose between 2 and 5.");
                return;
            }
            requiredPlayers = numPlayers;
        }

        // Add the player to the lobby
        waitingClients.put(nickname, callback);
        System.out.println("[LOBBY] " + nickname + " is connected. (" + waitingClients.size() + "/" + requiredPlayers + ")");

        // Notifies all clients in waiting
        notifyAllWaiting(false);

        // Check if lobby is full
        if (waitingClients.size() == requiredPlayers) {
            startGame();
        }
    }

    private void startGame() throws Exception {
        // Notify all clients that the game is starting (gameStarting = true)
        notifyAllWaiting(true);

        // Creates the list of Player from the model
        List<Player> players = new ArrayList<>();
        for (String nick : waitingClients.keySet()) {
            players.add(new Player(nick));
        }

        // Creates the Match and ServerController
        Match match = new Match(players);
        ServerController serverController = new ServerController(match);

        // Registers each client in the ServerController
        for (Map.Entry<String, ClientCallback> entry : waitingClients.entrySet()) {
            String nick = entry.getKey();
            ClientCallback callback = entry.getValue();
            RMIClientConnection connection = new RMIClientConnection(callback);
            serverController.registerClient(connection, nick);
        }

        // Send first snapshot to all clients
        serverController.sendInitialState();

        // Alerts GameServerImpl that the controller is ready
        onReady.onLobbyReady(serverController);
    }

    private void notifyAllWaiting(boolean gameStarting) {
        LobbyUpdateMessage update = new LobbyUpdateMessage(
                waitingClients.size(),
                requiredPlayers,
                new ArrayList<>(waitingClients.keySet()),
                gameStarting
        );
        for (ClientCallback cb : waitingClients.values()) {
            try {
                cb.receiveLobbyUpdate(update);
            } catch (Exception e) {
                System.err.println("[LOBBY] Errore notifica client: " + e.getMessage());
            }
        }
    }
}
