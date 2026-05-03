package org.example.client.view.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIController.GUIGameController;
import org.example.client.view.GUI.GUIController.GUILobbyController;
import org.example.client.view.GUI.registry.CardImageRegistry;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.client.view.UIHandler;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

import java.util.List;

public class GUIHandler implements UIHandler {
    private ClientController controller;
    private Stage stage;
    private String localNickname = controller.getNickname();

    private GUILobbyController GUILobbyController;
    private GUIGameController GUIGameController;

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
        Platform.runLater(() -> {

            if ( GUIGameController != null) {
                GUIGameController.update(update);
            } else {
                // Load game scene
                switchToGame(update);
            }

        });
    }


    @Override
    public void onError(String errorMessage, GamePhase currentPhase) {

    }


    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingMessage) {

    }

    @Override
    public void onShutdown() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Match Ended");
            alert.setHeaderText("Server closed the match.");
            alert.setContentText("Thanks for playing MESOS");
            alert.showAndWait();
            Platform.exit();
        });
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
            stage.setTitle("MESOS — Lobby");

        } catch (Exception e) {
            System.err.println("Failed to load lobby scene: " +  e.getMessage());
        }
    }


    private void switchToGame(GameStateUpdateMessage update) {
        // 1. Assign colors to players
        List<String> nicknames = update.getPlayers().stream()
                .map(p -> p.getNickname())
                .collect(java.util.stream.Collectors.toList());
        PlayerColorRegistry.getInstance().init(nicknames);

        // 2. Initialize CardImageRegistry with the number of players (to load correct card images)
        int numPlayers = update.getPlayers().size();
        CardImageRegistry.getInstance().init(numPlayers);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/game.fxml")
            );

            Parent root = loader.load();

            GUIGameController = loader.getController();
            GUIGameController.update(update);

            stage.setScene(new Scene(root));
            stage.setTitle("MESOS — Match");
            stage.setFullScreen(true);

        } catch (Exception e) {
            System.err.println("Failed to load game scene: " +  e.getMessage());

        }
    }
}
