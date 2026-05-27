package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIHandler;
import org.example.network.CommunicationProtocol;

/**
 * Controller for the login screen; validates input and starts the client.
 */
public class GUILoginController {

    // ── Main menu panel ─────────────────────────────────────────────────────────
    @FXML private VBox menuPanel;

    // ── Create lobby panel ──────────────────────────────────────────────────────
    @FXML private VBox createPanel;
    @FXML private TextField createNicknameField;
    @FXML private TextField createNumPlayersField;
    @FXML private Label createErrorLabel;

    // ── Join lobby panel ────────────────────────────────────────────────────────
    @FXML private VBox joinPanel;
    @FXML private TextField joinCodeField;
    @FXML private TextField joinNicknameField;
    @FXML private Label joinErrorLabel;

    // ── Connection config ───────────────────────────────────────────────────────
    private Stage stage;
    private String host = "localhost";
    private int port = 1099;
    private CommunicationProtocol protocol = CommunicationProtocol.RMI;

    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setProtocol(CommunicationProtocol p) { this.protocol = p; }
    public void setStage(Stage stage) { this.stage = stage; }

    // PANEL NAVIGATION

    @FXML
    private void onCreateSelected() {
        showPanel(createPanel);
    }

    @FXML
    private void onJoinSelected() {
        showPanel(joinPanel);
    }

    @FXML
    private void onBack() {
        showPanel(menuPanel);
    }

    // ACTIONS

    // Handles the "Create Lobby" action: validates input and attempts to connect to the server to create a new lobby
    @FXML
    private void onCreateLobby() {
        String nickname = createNicknameField.getText().trim();
        String numPlayersText = createNumPlayersField.getText().trim();

        if (nickname.isEmpty()) {
            showError(createErrorLabel, "Nickname cannot be empty");
            return;
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(numPlayersText);
        } catch (NumberFormatException e) {
            showError(createErrorLabel, "Invalid number format");
            return;
        }

        if (numPlayers < 2 || numPlayers > 5) {
            showError(createErrorLabel, "Number of players must be between 2 and 5");
            return;
        }

        GUIHandler gui = new GUIHandler();
        gui.setPrimaryStage(stage);
        gui.setConnectionInfo(host, port, protocol);
        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);

        try {
            hideError(createErrorLabel);
            showInfo(createErrorLabel, "Connecting...");
            controller.createLobbyAndConnect(host, port, numPlayers, protocol);
        } catch (Exception e) {
            showError(createErrorLabel, "Impossible to connect to server");
        }
    }

    // Handles the "Join Lobby" action: validates input and attempts to connect to the server to join an existing lobby
    @FXML
    private void onJoinLobby() {
        String code = joinCodeField.getText().trim();
        String nickname = joinNicknameField.getText().trim();

        if (code.isEmpty()) {
            showError(joinErrorLabel, "Game code cannot be empty");
            return;
        }
        if (nickname.isEmpty()) {
            showError(joinErrorLabel, "Nickname cannot be empty");
            return;
        }

        GUIHandler gui = new GUIHandler();
        gui.setPrimaryStage(stage);
        gui.setConnectionInfo(host, port, protocol);
        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);
        gui.setLobbyRetryEnabled(protocol == CommunicationProtocol.SOCKET);

        try {
            hideError(joinErrorLabel);
            showInfo(joinErrorLabel, "Connecting...");
            controller.joinLobbyAndConnect(host, port, code, protocol);
        } catch (Exception e) {
            showError(joinErrorLabel, "Invalid game code or server unreachable");
        }
    }

    // Utility method to show the join panel with a specific error message (used when redirected from lobby with an error)
    public void showJoinWithError(String error) {
        showPanel(joinPanel);
        showError(joinErrorLabel, error);
    }


    // PRIVATE HELPERS

    // Shows the specified panel and hides the others.
    private void showPanel(VBox panel) {
        for (VBox p : new VBox[]{menuPanel, createPanel, joinPanel}) {
            p.setVisible(p == panel);
            p.setManaged(p == panel);
        }
    }

    // Shows an error message in the specified label, styled in red.
    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 11px;");
        label.setVisible(true);
        label.setManaged(true);
    }

    // Shows an informational message in the specified label, styled in a neutral color.
    private void showInfo(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");
        label.setVisible(true);
        label.setManaged(true);
    }

    // Hides the specified label.
    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}