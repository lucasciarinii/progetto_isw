package org.example.server;

import org.example.network.HybridServerNetworkAdapter;
import org.example.network.ServerNotifier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.ThreadLocalRandom.current;

public class MatchManager {

    private static final int ID_LENGTH = 6;
    private static final String ALPHABET = "ABCDEFGHIJKLMOPQRSTUVWXYZ0123456789";


    private final LobbyReadyListener onReady;
    private final ServerNotifier notifier;
    private final HybridServerNetworkAdapter hybrid;

    private final Map<String, LobbyController> lobbies = new ConcurrentHashMap<>();
    private final Map<String, ServerController> games = new ConcurrentHashMap<>();
    private final Map<String, Thread> gameThreads = new ConcurrentHashMap<>();
    private final Set<String> globalNicknames = ConcurrentHashMap.newKeySet();


    public MatchManager(LobbyReadyListener onReady, ServerNotifier notifier, HybridServerNetworkAdapter hybrid) {
        this.onReady = onReady;
        this.notifier = notifier;
        this.hybrid = hybrid;
    }

    public String createLobby(String nickname, int numPlayers) throws Exception {

        // Create a unique key
        String gameID = generateUniqueID();

        // Create a lobby
        LobbyController newLobby = new LobbyController(onReady, notifier, gameID, numPlayers);

        // Add the player to the lobby
        newLobby.registerPlayer(nickname);
        reserveNickname(nickname);

        // Add the lobby to the hashmap
        lobbies.put(gameID, newLobby);
        hybrid.registerPlayerGameID(nickname, gameID);
        return gameID;
    }


    public void joinLobby(String nickname, String gameID) throws Exception{
        String cleanID = gameID.trim().toUpperCase();

        // Retrieve lobby controller from map
        LobbyController lobbyController = lobbies.get(cleanID);
        if ( lobbyController == null ) {
            throw new IllegalStateException("Lobby not found");
        }

        if ( lobbyController.isNicknameTaken(nickname) ) {
            throw new IllegalArgumentException("Nickname already taken");
        }

        // Register the player in the lobby
        lobbyController.registerPlayer(nickname);
        reserveNickname(nickname);
        hybrid.registerPlayerGameID(nickname, cleanID);
    }

    public void onLobbyReady(String gameID, ServerController serverController) {
        String cleanID = gameID.trim().toUpperCase();

        // 1. Remove gameID from lobbies and add it to games
        lobbies.remove(cleanID);
        games.put(cleanID, serverController);

        serverController.setGameOverListener(_ -> this.onGameOver(cleanID));

        // 2. Create dedicated thread using Runnable interface in ServerController
        Thread gameThread = new Thread(serverController, "GameThread-" + cleanID);
        gameThreads.put(cleanID, gameThread);

        // 5. Start the thread
        gameThread.start();
    }


    public void onGameOver(String gameID) {
        String cleanID = gameID.trim().toUpperCase();

        ServerController serverController = games.get(cleanID);
        serverController.getPlayers().forEach(p -> globalNicknames.remove(p.getNickname()));

        games.remove(cleanID);
        gameThreads.remove(cleanID);
    }

    //! UTILITY METHODS -------------------------------------------------------------------------------------------------
    // The method generates an uniqueID without collisions with ID of already existing games or lobbies
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

    private void reserveNickname(String nickname) {
        if (!globalNicknames.add(nickname)) {
            throw new IllegalArgumentException("Nickname already used: " + nickname);
        }
    }


}
