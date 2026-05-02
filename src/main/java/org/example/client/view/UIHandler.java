package org.example.client.view;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public interface UIHandler {

    public void onLobbyUpdate(LobbyUpdateMessage update);
    public void onUpdate(GameStateUpdateMessage update);
    public void onError(String errorMessage);
    public void onRankingUpdate(RankingUpdateMessage rankingMessage);
    public void onShutdown();
    public void promptForAction(GamePhase phase);
    public void displayNoCardsPickable();
    public void displayWaiting(String currentPlayerNickname);

}
