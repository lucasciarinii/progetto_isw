package org.example.network;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;

public interface ServerNetworkAdapter {

    // Server starts on a port
    void start(int port) throws Exception;

    // Server stops
    void stop() throws Exception;

    // Server sends a lobby update message to all clients
    void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception;

    // Server sends a game state update message to all clients
    void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception;

    // Server sends an error message to all clients
    void sendError(String nickname, String errorMessage) throws Exception;

    // Server sends a ranking update message to all clients
    void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception;

    // Server notifies the shutdown to all clients
    void sendShutdown(String nickname) throws Exception;
}
