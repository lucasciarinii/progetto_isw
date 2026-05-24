package org.example.server;

/**
 * Controller that manages the lobby before the game starts.
 * Lobby is created with already decided number of players (2-5), and once
 * the lobby is full a match and its controller are created.
 */

import org.example.network.ServerNotifier;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class LobbyController {


    // Keeps the order of connection: nicknames
    private final LinkedHashSet<String> waitingClients = new LinkedHashSet<>();

    // Callback invoked when the lobby is full.
    private final LobbyReadyListener onReady;
    private final ServerNotifier notifier;
    private final String gameID;
    private final int numPlayers;
    private boolean started = false;

    public LobbyController(LobbyReadyListener onReady, ServerNotifier notifier, String gameID, int numPlayers) {
        this.onReady = onReady;
        this.notifier = notifier;
        this.gameID = gameID;
        this.numPlayers = numPlayers;
    }

    /* Registers a new player in the lobby
        * @param nickname       player's nickname
        * @param numPlayers     desired number of players (used only by the first one)
        * @param callback       RMI callback to communicate with this client
    */
    public synchronized void registerPlayer(String nickname) throws Exception {

        if (started) {
            throw new IllegalStateException("Lobby already started");
        }
        if (waitingClients.size() >= numPlayers) {
            throw new IllegalStateException("Lobby already full");
        }

        // Check if the nickname is already taken
        if (waitingClients.contains(nickname)) {
            throw new IllegalArgumentException("Nickname already used: " + nickname);
        }

        // Add the player to the lobby
        waitingClients.add(nickname);
        ServerLogger.lobby(nickname + " joined lobby with code: " + gameID + " (" + waitingClients.size() + "/" + numPlayers + ")");

        // Notifies all clients in waiting
        notifyAllWaiting(false);

        // Check if lobby is full
        if (waitingClients.size() == numPlayers) {
            startGame();
        }
    }

    private synchronized void startGame() throws Exception {
        if (started) {
            return;
        }
        started = true;

        // Notify all clients that the game is starting (gameStarting = true)
        notifyAllWaiting(true);

        // Creates the list of Player from the model
        List<Player> players = new ArrayList<>();
        for (String nick : waitingClients) {
            players.add(new Player(nick));
        }

        // Creates the Match and ServerController
        Match match = new Match(players);
        ServerController serverController = new ServerController(match, notifier);

        // Alerts GameServerImpl that the controller is ready
        onReady.onLobbyReady(serverController, gameID);
        ServerLogger.game("Game started with code: " + gameID);
    }

    /**
     * Sends the current lobby status to all waiting clients.
     *
     * @param gameStarting whether the game is starting now
     */
    private void notifyAllWaiting(boolean gameStarting) {
        LobbyUpdateMessage update = new LobbyUpdateMessage(
                waitingClients.size(),
                numPlayers,
                new ArrayList<>(waitingClients),
                gameStarting,
                gameID
        );
        for (String nickname : waitingClients) {
            try {
                notifier.sendLobbyUpdate(nickname, update);
            } catch (Exception e) {
                ServerLogger.lobby("Failed to notify " + nickname + ": " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether a nickname is already present in the lobby.
     *
     * @param nickname the nickname to verify
     * @return true if the nickname is already taken
     */
    public synchronized boolean isNicknameTaken(String nickname) {
        return waitingClients.contains(nickname);
    }

    public synchronized boolean isStarted() {
        return started;
    }
}
