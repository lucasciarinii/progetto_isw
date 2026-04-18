package org.example.server;

//? Callback invoked by LobbyController when the lobby is full
public interface LobbyReadyListener {
    void onLobbyReady(ServerController serverController);
}
