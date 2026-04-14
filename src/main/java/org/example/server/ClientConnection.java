package org.example.server;

// This interface is used by the ServerController to talk with a client. It's technology-agnostic
// We can have different implementations:
//      - RMIClientConnection
//      - SocketClientConnection

import org.example.network.GameStateUpdateMessage;

public interface ClientConnection {
    // Sends complete state snapshot to the client (after each valid move)
    void sendUpdate(GameStateUpdateMessage update) throws Exception;

    // Sends an error to the client (e.g., invalid move, wrong turn, etc.)
    void sendError(String errorMessage) throws Exception;
}
