package it.polimi.ingsw.client;

import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.server.model.enums.GamePhase;

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
