package it.polimi.ingsw.server;

import it.polimi.ingsw.network.ServerNotifier;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;
import it.polimi.ingsw.server.model.enums.GamePhase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class LobbyController {

    // Keeps the order of connection: nicknames
    private final LinkedHashSet<String> waitingClients = new LinkedHashSet<>();

    private final LobbyReadyListener onReady;
    private final ServerNotifier notifier;
    private final String gameID;
    private final int numPlayers;
    private boolean started = false;

    /**
     * Creates a lobby controller for a specific game session.
     * The lobby collects players until the configured number is reached, then
     * creates the corresponding match and server controller.
     *
     * @param onReady listener notified when the lobby is full and the game can start
     * @param notifier network notifier used to send lobby updates to clients
     * @param gameID the identifier associated with this lobby
     * @param numPlayers the number of players required to start the game
     */
    public LobbyController(LobbyReadyListener onReady, ServerNotifier notifier, String gameID, int numPlayers) {
        this.onReady = onReady;
        this.notifier = notifier;
        this.gameID = gameID;
        this.numPlayers = numPlayers;
    }

    /** Registers a new player in the lobby
        * @param nickname player's nickname
    */
    public synchronized void registerPlayer(String nickname) {

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

    /**
     * Marks the lobby as started, notifies all waiting clients that the match is
     * beginning, creates the model-side players and match, and instantiates the
     * corresponding server controller.
     */
    private synchronized void startGame() {
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

    /**
     * Returns whether this lobby has already transitioned to a started game.
     *
     * @return {@code true} if the lobby has started, {@code false} otherwise
     */
    public synchronized boolean isStarted() {
        return started;
    }

    /**
     * Returns a snapshot of the nicknames currently waiting in the lobby
     *
     * @return a copy of the waiting players' nicknames
     */    public synchronized List<String> getWaitingNicknames() {
        return new ArrayList<>(waitingClients);
    }

    /**
     * Cancels the lobby and notifies all waiting clients with an error followed
     * by a shutdown event.
     *
     * @param reason the reason for the lobby cancellation
     */
    public synchronized void cancelLobby(String reason) {
        for (String nickname : waitingClients) {
            try {
                notifier.sendError(nickname, reason, GamePhase.LOBBY);
                notifier.sendShutdown(nickname);
            } catch (Exception e) {
                ServerLogger.lobby("Failed to notify " + nickname + " about lobby cancellation: " + e.getMessage());
            }
        }
    }
}
