package org.example.client;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

/**
 * Listener for server-driven events, used to handle network callbacks
 * from the client controller.
 */
public interface GameEventListener {
    void onUpdate(GameStateUpdateMessage update);

    void onError(String errorMessage, GamePhase phase);

    void onLobbyUpdate(LobbyUpdateMessage update);

    void onRankingUpdate(RankingUpdateMessage rankingUpdate);

    void onRoundFlowCardRequest();

    void onShutdown();
}
