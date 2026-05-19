package org.example.server;

/**
 * Controller that manages the lobby before the game starts.
 * The first player selects the total number of players (2-5), and once
 * the lobby is full a match and its controller are created.
 */

import org.example.network.ServerNotifier;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class LobbyController implements GameOverListener {
    private int requiredPlayers = -1;  // -1 = not decided yet

    // Keeps the order of connection: nicknames
    private final LinkedHashSet<String> waitingClients = new LinkedHashSet<>();

    // Callback invoked when the lobby is full.
    private final LobbyReadyListener onReady;
    private final ServerNotifier notifier;

    public LobbyController(LobbyReadyListener onReady, ServerNotifier notifier) {
        this.onReady = onReady;
        this.notifier = notifier;
    }

    /**
     * Registers a player in the lobby.
     * The first player sets the desired total number of players.
     *
     * @param nickname   the player's nickname
     * @param numPlayers desired number of players (used only by the first one)
     * @throws Exception if the nickname is already taken or the lobby is invalid
     */
    public synchronized void registerPlayer(String nickname, int numPlayers) throws Exception {

        // Check if the nickname is already taken
        if (waitingClients.contains(nickname)) {
            throw new IllegalArgumentException("Nickname already used: " + nickname);
        }

        // First player decides how many players will be in the game (2-5)
        if (waitingClients.isEmpty()) {
            if (numPlayers < 2 || numPlayers > 5) {
                throw new IllegalArgumentException("Invalid number of players. Choose between 2 and 5.");
            }
            requiredPlayers = numPlayers;
        }

        // Add the player to the lobby
        waitingClients.add(nickname);
        ServerLogger.lobby(nickname + " joined the lobby. (" + waitingClients.size() + "/" + requiredPlayers + ")");

        // Notifies all clients in waiting
        notifyAllWaiting(false);

        // Check if lobby is full
        if (waitingClients.size() == requiredPlayers) {
            startGame();
        }
    }

    /**
     * Initializes the match and notifies clients that the game is starting.
     * - Creates the list of Players from the waiting clients connected to the lobby
     * - Creates the Match (model) and ServerController (controller)
     * - Sends the first snapshot to all clients
     */
    private void startGame() throws Exception {
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
        serverController.setGameOverListener(this);

        // Send first snapshot to all clients
        serverController.sendInitialState();

        // Alerts GameServerImpl that the controller is ready
        onReady.onLobbyReady(serverController);
        ServerLogger.game("Game started with mixed connections.");
    }

    /**
     * Sends the current lobby status to all waiting clients.
     *
     * @param gameStarting whether the game is starting now
     */
    private void notifyAllWaiting(boolean gameStarting) {
        LobbyUpdateMessage update = new LobbyUpdateMessage(
                waitingClients.size(),
                requiredPlayers,
                new ArrayList<>(waitingClients),
                gameStarting
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
    public boolean isNicknameTaken(String nickname) {
        return waitingClients.contains(nickname);
    }

    /**
     * Resets the lobby state when a match ends.
     *
     * @param controller the controller that finished the match
     */
    @Override
    public void onGameOver(ServerController controller) {
        // Reset lobby state for a new match
        waitingClients.clear();
        requiredPlayers = -1;
        ServerLogger.lobby("Game over. Lobby ready for new game.");
    }
}
