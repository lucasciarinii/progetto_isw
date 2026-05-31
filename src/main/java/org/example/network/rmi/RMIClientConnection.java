package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;
import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

/**
 * Adapter that bridges ServerNotifier calls to the client's RMI callback.
 * The server uses this connection to send updates and errors to the client.
 */
public class RMIClientConnection implements ServerNotifier {
    private final RMIClientCallback callback;

    /**
     * Creates a connection bound to the client callback stub.
     *
     * @param callback the remote callback implemented by the client
     */
    public RMIClientConnection(RMIClientCallback callback) {
        this.callback = callback;
    }

    public RMIClientCallback getCallback(){
        return callback;
    }

    /**
     * Forwards a game state update to the client callback.
     */
    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        callback.receiveUpdate(update);
    }

    /**
     * Forwards an error to the client callback.
     */
    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        callback.receiveError(errorMessage, phase);
    }

    /**
     * Forwards a ranking update to the client callback.
     */
    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage rankingUpdate) throws Exception {
        callback.receiveRankingUpdate(rankingUpdate);
    }

    /**
     * Forwards a RoundFlow request to the client callback.
     */
    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        callback.receiveRoundFlowCardRequest();
    }

    /**
     * Forwards a shutdown notification to the client callback.
     */
    @Override
    public void sendShutdown(String nickname) throws Exception {
        callback.receiveShutdown();
    }

    /**
     * Forwards a lobby update to the client callback.
     */
    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        callback.receiveLobbyUpdate(update);
    }

    @Override
    public void handleClientDisconnect(String nickname, String reason) {
        // No-op: RMI callbacks do not need server-side disconnect handling here.
    }
}
