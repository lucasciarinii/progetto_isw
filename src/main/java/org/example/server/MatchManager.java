package org.example.server;

import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNotifier;

import java.net.NoRouteToHostException;
import java.rmi.ConnectException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.ThreadLocalRandom.current;

public class MatchManager {

    private static final int ID_LENGTH = 6;
    private static final String ALPHABET = "ABCDEFGHIJKLMPQRSTUVWXYZ123456789";


    private final LobbyReadyListener onReady;
    private final ServerNotifier notifier;
    private final HybridServerNetworkAdapter hybrid;

    private final Map<String, LobbyController> lobbies = new ConcurrentHashMap<>();
    private final Map<String, ServerController> games = new ConcurrentHashMap<>();

    private final Map<String, Thread> gameThreads = new ConcurrentHashMap<>();
    private final Set<String> globalNicknames = ConcurrentHashMap.newKeySet();

    /**
     * Creates a match manager responsible for lobby lifecycle, active matches,
     * and nickname reservation across all game sessions.
     *
     * @param onReady listener notified when a lobby becomes ready to start
     * @param notifier notifier used to send events to connected clients
     * @param hybrid hybrid adapter used for player-to-game routing and cleanup
     */
    public MatchManager(LobbyReadyListener onReady, ServerNotifier notifier, HybridServerNetworkAdapter hybrid) {
        this.onReady = onReady;
        this.notifier = notifier;
        this.hybrid = hybrid;
    }

    /**
     * Returns {@code true} if the given string is {@code null} or contains only whitespace.
     *
     * @param value the string to check
     * @return {@code true} if the string is blank, {@code false} otherwise
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Creates a new lobby, reserves the creator's nickname, and registers the
     * first player in the newly created session.
     *
     * @param nickname   the nickname of the player creating the lobby
     * @param numPlayers the number of players required to start the game
     * @return the generated game session ID
     */
    public String createLobby(String nickname, int numPlayers) {

        if (isBlank(nickname)) {
            throw new IllegalArgumentException("Nickname cannot be empty");
        }

        // Create a unique key
        String gameID = generateUniqueID();

        // Reserve the nickname
        reserveNickname(nickname);

        ServerLogger.lobby("Lobby created. Game code: " + gameID + " by " + nickname);

        // Create a lobby
        LobbyController newLobby = new LobbyController(onReady, notifier, gameID, numPlayers);

        try {
            // Add the player to the lobby
            newLobby.registerPlayer(nickname);
        } catch (Exception e) {
            globalNicknames.remove(nickname);
            throw e;
        }

        // Add the lobby to the hashmap
        lobbies.put(gameID, newLobby);
        hybrid.registerPlayerGameID(nickname, gameID);
        return gameID;
    }

    /**
     * Registers a player into an existing lobby after validating the lobby state
     * and reserving the player's nickname globally.
     *
     * @param nickname the nickname of the joining player
     * @param gameID the identifier of the lobby to join
     * @throws Exception if the lobby is invalid, already started, the nickname is taken,
     *                   or player registration fails
     */
    public void joinLobby(String nickname, String gameID) throws Exception{
        if (isBlank(gameID)) {
            throw new IllegalArgumentException("Game code cannot be empty");
        }
        if (isBlank(nickname)) {
            throw new IllegalArgumentException("Nickname cannot be empty");
        }

        String cleanID = gameID.trim().toUpperCase();

        // If game already started, reject with an error
        if (games.containsKey(cleanID)) {
            throw new IllegalStateException("Lobby already started");
        }

        // If the lobby does not exist, reject with an error
        LobbyController lobbyController = lobbies.get(cleanID);
        if ( lobbyController == null ) {
            throw new IllegalStateException("Lobby not found or already finished");
        }

        if (lobbyController.isStarted()) {
            throw new IllegalStateException("Lobby already started");
        }
        if ( lobbyController.isNicknameTaken(nickname) ) {
            throw new IllegalArgumentException("Nickname already taken");
        }

        reserveNickname(nickname);

        try {
            // Register the player in the lobby
            lobbyController.registerPlayer(nickname);
        } catch (Exception e) {
            globalNicknames.remove(nickname);
            throw e;
        }
        hybrid.registerPlayerGameID(nickname, cleanID);
    }

    /**
     * Moves a ready lobby into the active games registry and starts its dedicated
     * server-controller thread.
     *
     * @param gameID           the identifier of the ready game session
     * @param serverController the controller managing the started match
     */
    public void onLobbyReady(String gameID, ServerController serverController) {
        String cleanID = gameID.trim().toUpperCase();

        // 1. Remove gameID from lobbies and add it to games
        lobbies.remove(cleanID);
        games.put(cleanID, serverController);

        ServerLogger.game("Game session starting with code: " + cleanID);

        serverController.setGameOverListener(_ -> this.onGameOver(cleanID));

        // 2. Create dedicated thread using Runnable interface in ServerController
        Thread gameThread = new Thread(serverController, "GameThread-" + cleanID);
        gameThreads.put(cleanID, gameThread);

        // 5. Start the thread
        gameThread.start();
    }

    /**
     * Cleans up the registries associated with a completed game session and releases
     * the nicknames of all participating players.
     *
     * @param gameID the identifier of the completed game session
     */
    public void onGameOver(String gameID) {
        String cleanID = gameID.trim().toUpperCase();

        ServerController serverController = games.get(cleanID);
        if (serverController != null) {
            serverController.getPlayers().forEach(p -> globalNicknames.remove(p.getNickname()));
        }

        Thread gameThread = gameThreads.remove(cleanID);
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
        ServerLogger.game("Game session ended with code: " + cleanID);
    }

    /**
     * Aborts a lobby or a running game session, notifies the remaining players when possible,
     * and releases all associated shared state such as nicknames, mappings, and threads.
     *
     * @param gameID the identifier of the session to abort
     * @param reason the reason for the abort
     * @param disconnectedNickname the nickname of the player that triggered the abort,
     *                             or {@code null} if not applicable
     */
    public void abortGame(String gameID, String reason, String disconnectedNickname) {
        String cleanID = gameID.trim().toUpperCase();
        String cleanDisconnected = isBlank(disconnectedNickname) ? null : disconnectedNickname.trim();

        LobbyController lobby = lobbies.remove(cleanID);
        if (lobby != null) {
            // Lobby not started yet: free nicknames and notify everyone waiting.
            for (String nick : lobby.getWaitingNicknames()) {
                globalNicknames.remove(nick);
                hybrid.removePlayerMapping(nick);
            }
            lobby.cancelLobby(reason);
            ServerLogger.lobby("Lobby cancelled with code: " + cleanID + " (" + reason + ")");
            return;
        }

        ServerController serverController = games.remove(cleanID);
        if (serverController != null) {
            // Match already started: notify all players and cleanup shared state.
            serverController.getPlayers().forEach(p -> {
                String nick = p.getNickname();
                boolean skipNotify = cleanDisconnected != null && cleanDisconnected.equals(nick);
                try {
                    if (!skipNotify) {
                        notifier.sendError(nick, reason, null);
                        notifier.sendShutdown(nick);
                    }
                } catch (ConnectException | NoRouteToHostException e) {
                    ServerLogger.game("Player " + nick + " already unreachable, skipping notification");
                } catch (Exception e) {
                    ServerLogger.game("Failed to notify " + nick + " about disconnection: " + e.getMessage());
                } finally {
                    globalNicknames.remove(nick);
                    hybrid.removePlayerMapping(nick);
                }
            });
            Thread gameThread = gameThreads.remove(cleanID);
            if (gameThread != null && gameThread.isAlive()) {
                gameThread.interrupt();
            }
            ServerLogger.game("Game session aborted with code: " + cleanID + " (" + reason + ")");
        }
    }

    //! UTILITY METHODS -------------------------------------------------------------------------------------------------
    /**
     * Generates a unique game session identifier that does not collide with any
     * existing lobby or running match.
     *
     * @return a unique game session ID
     * @throws IllegalStateException if a unique ID cannot be generated after many attempts
     */
    private String generateUniqueID() {
        String newID;
        int attempts = 0;

        do {
            StringBuilder sb = new StringBuilder(ID_LENGTH);
            for (int i = 0; i < ID_LENGTH; i++) {
                int randomIndex = current().nextInt(ALPHABET.length());
                sb.append(ALPHABET.charAt(randomIndex));
            }
            newID = sb.toString();
            attempts++;

            if (attempts > 1000) {
                throw new IllegalStateException("Unable to generate a unique ID. Server capacity reached.");
            }
            // Check if the lobby or game contains the generated ID
        } while (lobbies.containsKey(newID) || games.containsKey(newID));

        return newID;
    }

    /**
     * Reserves a nickname globally across all lobbies and active matches.
     *
     * @param nickname the nickname to reserve
     * @throws IllegalArgumentException if the nickname is already in use
     */
    private void reserveNickname(String nickname) {
        if (!globalNicknames.add(nickname)) {
            throw new IllegalArgumentException("Nickname already used: " + nickname);
        }
    }


}
