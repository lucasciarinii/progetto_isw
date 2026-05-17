package org.example.client.view.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIController.GUIGameController;
import org.example.client.view.GUI.GUIController.GUILobbyController;
import org.example.client.view.GUI.GUIController.GUIRankingController;
import org.example.client.view.GUI.registry.CardImageRegistry;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.client.view.UIHandler;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.enums.GamePhase;

import java.util.List;

public class GUIHandler implements UIHandler {
    private ClientController controller;
    private Stage stage;

    private GUILobbyController GUILobbyController;
    private GUIGameController GUIGameController;
    private GUIRankingController GUIRankingController;
    private GameStateUpdateMessage lastGameUpdate; // ← aggiunge questo campo

    // SETTERS ---------------------------------------------------------------------------------------------------------

    public void setController(ClientController controller) {
        this.controller = controller;
        init();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
        this.stage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });
    }


    // -----------------------------------------------------------------------------------------------------------------

    private void init() {
    }

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
            this.lastGameUpdate = update;
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
        Platform.runLater(() -> {
            if (GUIGameController != null) {
                GUIGameController.showError(errorMessage);
            }
        });
    }


    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingMessage) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/ranking.fxml")
                );
                Parent root = loader.load();

                GUIRankingController = loader.getController();
                GUIRankingController.populate(
                        rankingMessage,
                        lastGameUpdate != null ? lastGameUpdate.getPlayers() : List.of()
                );

                stage.setScene(new Scene(root));
                stage.setTitle("MESOS — Results");
                stage.setFullScreen(true);

            } catch (Exception e) {
                System.err.println("Failed to load ranking scene: " + e.getMessage());
            }
        });
    }


    @Override
    public void onRoundFlowCardRequest() {
//        Platform.runLater(() -> {
//            if (GUIGameController != null) {
//                GUIGameController.promptRoundFlowPick();
//            }
//        });
    }

    @Override
    public void onShutdown() {
        Platform.runLater(() -> {
            if (GUIRankingController != null) {
                GUIRankingController.showClosingMessage();
            }
        });
    }

    @Override
    public void promptForAction(GamePhase phase) {
        Platform.runLater(() -> {
            if (GUIGameController != null) {
                GUIGameController.promptForAction(phase);
            }
        });
    }

    @Override
    public void displayNoCardsPickable() {
        Platform.runLater(() -> {
            if (GUIGameController != null) {
                GUIGameController.showNoCardsPickable();
            }
        });
    }

    @Override
    public void displayWaiting(String currentPlayerNickname) {
        Platform.runLater(() -> {
            if (GUIGameController != null) {
                GUIGameController.showWaiting(currentPlayerNickname);
            }
        });
    }

    @Override
    public void displayRoundFlowWaiting(String currentPlayerNickname) {
        Platform.runLater(() -> {
            if (GUIGameController != null) {
                GUIGameController.showRoundFlowWaiting(currentPlayerNickname);
            }
        });
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
            stage.setResizable(false);
            stage.setWidth(520);
            stage.setHeight(420);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Failed to load lobby scene: " +  e.getMessage());
        }
    }


    private void switchToGame(GameStateUpdateMessage update) {
        // 1. Assign colors to players
        List<String> nicknames = update.getPlayers().stream()
                .map(PlayerSnapshot::getNickname)
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
            GUIGameController.setController(controller);
            GUIGameController.setLocalNickname(controller.getNickname());
            GUIGameController.update(update);

            stage.setScene(new Scene(root));
            stage.setTitle("MESOS — Match");

            stage.setResizable(true);
            stage.show();

            Platform.runLater(() -> {
                if (stage.isShowing()) {
                    stage.setFullScreen(true);
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to load game scene: " +  e.getMessage());

        }
    }
}
