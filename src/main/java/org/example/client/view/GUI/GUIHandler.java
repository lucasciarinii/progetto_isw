package org.example.client.view.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private GUILobbyController GUILobbyController;

    // SETTERS ---------------------------------------------------------------------------------------------------------

    public void setController(ClientController controller) {
        this.controller = controller;
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        Platform.runLater(() -> {
            if (GUILobbyController != null) {
                GUILobbyController.update(update);
            }
            else {
                switchToLobby(update);
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


    // -----------------------------------------------------------------------------------------------------------------

    private void switchToLobby(LobbyUpdateMessage update) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/lobby.fxml")
            );

            Parent root = loader.load();

            GUILobbyController = loader.getController();
            GUILobbyController.update(update);
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            System.err.println("Failed to load lobby scene: " +  e.getMessage());
        }
    }
}
