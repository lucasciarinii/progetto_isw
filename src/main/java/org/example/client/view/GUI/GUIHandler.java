package org.example.client.view.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIController.GUIGameController;
import org.example.client.view.GUI.GUIController.GUILobbyController;
import org.example.client.view.GUI.GUIController.GUILoginController;
import org.example.client.view.GUI.GUIController.GUIRankingController;
import org.example.client.view.GUI.registry.CardImageRegistry;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.client.view.UIHandler;
import org.example.network.CommunicationProtocol;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.enums.GamePhase;

import java.util.List;

/**
 * JavaFX UI handler for GUI VIEW that switches scenes and forwards updates to GUI controllers.
 */
public class GUIHandler implements UIHandler {
    private ClientController controller;
    private Stage stage;

    private String host;
    private CommunicationProtocol protocol;

    private GUILobbyController GUILobbyController;
    private GUIGameController GUIGameController;
    private GUIRankingController GUIRankingController;
    private GameStateUpdateMessage lastGameUpdate;
    private boolean lobbyRetryEnabled = false;
//    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String gameID;


    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.stage = primaryStage;
        this.stage.setOnCloseRequest(_ -> {
            // Try to notify the server before closing the app.
            if (controller != null) {
                controller.disconnect();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    public void setLobbyRetryEnabled(boolean enabled) {
        this.lobbyRetryEnabled = enabled;
    }

    public void setConnectionInfo(String host, CommunicationProtocol protocol) {
        this.host = host;
        this.protocol = protocol;
    }

    @Override
    public void setGameID(String gameID) {
        this.gameID = gameID;
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
            if (currentPhase == GamePhase.LOBBY && lobbyRetryEnabled) {
                // Go back to login-screen to retry joining
                switchToLogin("Invalid game code or nickname: " + errorMessage);
                return;
            }
            if (currentPhase == GamePhase.GAME_ABORTED) {
                if (GUIGameController != null) {
                    GUIGameController.showGameAborted();
                }
                // If the game scene was never shown, avoid triggering UI errors.
                return;
            }
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
        // This method is intentionally left empty, as the card picking logic is handled in the GUIGameController when the GameStateUpdateMessage is received
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


    /**
     * Switches the GUI to the login scene and shows the join panel with
     * the provided error message.
     *
     * @param errorMessage the error message to display in the login scene
     */
    private void switchToLogin(String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();

            GUILoginController loginController = loader.getController();
            loginController.setHost(host);
            loginController.setProtocol(protocol);
            loginController.setStage(stage);

            // Show directly the join panel with the error
            loginController.showJoinWithError(errorMessage);

            stage.setScene(new Scene(root));
            stage.setTitle("MESOS — Login");
            stage.setResizable(false);
            stage.setWidth(1376);
            stage.setHeight(768);
            GUILobbyController = null;

        } catch (Exception e) {
            System.err.println("Failed to reload login scene: " + e.getMessage());
        }
    }

    /**
     * Switches the GUI to the lobby scene and renders the provided lobby state.
     *
     * @param update the lobby update to display
     */
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
            stage.setWidth(1376);
            stage.setHeight(768);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Failed to load lobby scene: " +  e.getMessage());
        }
    }

    /**
     * Switches the GUI to the game scene, initializes shared GUI registries,
     * and renders the provided game state.
     *
     * @param update the game state update to display
     */
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
