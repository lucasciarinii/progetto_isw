package org.example.network.rmi;

import com.mysql.cj.protocol.ServerCapabilities;
import org.example.client.rmi.RMIClientCallback;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.server.ServerNotifier;
import org.example.server.model.enums.GamePhase;

//? The "adapter": it let RMI talk with ServerController
//? RMI Implementation of ClientConnection interface. This is the object that the server will use to send updates and errors to the client, by calling the methods of the ClientCallback interface implemented by the client.
public class RMIClientConnection implements ServerNotifier {
    private final RMIClientCallback callback;

    public RMIClientConnection(RMIClientCallback callback) {
        this.callback = callback;
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        callback.receiveUpdate(update);
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        callback.receiveError(errorMessage, phase);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage rankingUpdate) throws Exception {
        callback.receiveRankingUpdate(rankingUpdate);
    }

    @Override
    public void sendRoundFlowCardRequest(String nickname) throws Exception {
        callback.receiveRoundFlowCardRequest();
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        callback.receiveShutdown();
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        callback.receiveLobbyUpdate(update);
    }
}
