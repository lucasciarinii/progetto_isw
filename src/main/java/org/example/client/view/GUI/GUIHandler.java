package org.example.client.view.GUI;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIController.GUILobbyController;
import org.example.client.view.UIHandler;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public class GUIHandler implements UIHandler {
    private ClientController controller;
    private Stage stage;

    private GUILobbyController lobbyController;

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        Platform.runLater(() -> {
            if (lobbyController != null) {
                //lobbyController.update(update);
            }
            else {

            }
        });
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
