package org.example.client.view;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public interface UIHandler {

    void onLobbyUpdate(LobbyUpdateMessage update);
    void onGameStateUpdate(GameStateUpdateMessage update);
    void onError(String errorMessage, GamePhase currentPhase);
    void onRankingUpdate(RankingUpdateMessage rankingMessage);
    void onRoundFlowCardRequest();
    void onShutdown();
    void promptForAction(GamePhase phase);
    void displayNoCardsPickable();
    void displayWaiting(String currentPlayerNickname);
    void displayRoundFlowWaiting(String currentPlayerNickname);

}
