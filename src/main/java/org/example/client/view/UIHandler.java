package org.example.client.view;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public interface UIHandler {

    public void onLobbyUpdate(LobbyUpdateMessage update);
    public void onGameStateUpdate(GameStateUpdateMessage update);
    public void onError(String errorMessage, GamePhase currentPhase);
    public void onRankingUpdate(RankingUpdateMessage rankingMessage);
    public void onShutdown();
    public void promptForAction(GamePhase phase);
    public void displayNoCardsPickable();
    public void displayWaiting(String currentPlayerNickname);

}
