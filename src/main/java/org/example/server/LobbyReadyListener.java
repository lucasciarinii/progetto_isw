package org.example.server;

/**
 * Callback invoked by LobbyController when the lobby is full.
 */
public interface LobbyReadyListener {
    /**
     * Called when a lobby is ready and a controller can be used by the server.
     *
     * @param serverController the initialized controller for the new match
     */
    void onLobbyReady(ServerController serverController);
}
