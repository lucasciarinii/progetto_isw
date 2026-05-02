package org.example.client.view.GUI;

import org.example.client.ClientController;
import org.example.client.view.UIHandler;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public class GUIHandler implements UIHandler {
    private ClientController controller;

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {

    }

    @Override
    public void onGameStateUpdate(GameStateUpdateMessage update) {

    }

    @Override
    public void onError(String errorMessage, GamePhase currentPhase) {

    }

    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingMessage) {

    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void promptForAction(GamePhase phase) {

    }

    @Override
    public void displayNoCardsPickable() {

    }

    @Override
    public void displayWaiting(String currentPlayerNickname) {

    }
}
