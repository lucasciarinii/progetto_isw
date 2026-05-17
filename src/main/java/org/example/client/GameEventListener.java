package org.example.client;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

//? This interfaces decouples the ClientCallbackImpl from the ClientController. Whoever wants to receive updates or errors from server implements this interface.
public interface GameEventListener {

    void onUpdate(GameStateUpdateMessage update);

    void onError(String errorMessage, GamePhase phase);

    void onLobbyUpdate(LobbyUpdateMessage update);

    void onRankingUpdate(RankingUpdateMessage rankingUpdate);

    void onRoundFlowCardRequest();

    void onShutdown();
}
