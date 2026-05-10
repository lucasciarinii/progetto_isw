package org.example.server;

// This interface is used by the ServerController to talk with a client. It's technology-agnostic
// We can have different implementations:
//      - RMIClientConnection
//      - SocketClientConnection

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public interface ClientConnection {
    // Sends complete state snapshot to the client (after each valid move)
    void sendGameStateUpdate(GameStateUpdateMessage update) throws Exception;

    // Sends an error to the client (e.g., invalid move, wrong turn, etc.)
    void sendError(String errorMessage, GamePhase phase) throws Exception;

    // Sends ranking update
    void sendRankingUpdate(RankingUpdateMessage rankingUpdate) throws Exception;

    // Sends shutdown message
    void sendShutdown() throws Exception;
}
