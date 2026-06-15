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
 * Controller for the login screen; validates input and starts the client
 */
public class GUILoginController {

    /** Main menu panel shown before selecting create or join lobby */
    @FXML private VBox menuPanel;

    /** Panel containing the controls for lobby creation */
    @FXML private VBox createPanel;

    /** Text field used to enter the nickname for lobby creation */
    @FXML private TextField createNicknameField;

    /** Text field used to enter the desired number of players */
    @FXML private TextField createNumPlayersField;

    /** Label used to display validation or connection errors in create mode */
    @FXML private Label createErrorLabel;

    /** Panel containing the controls for joining an existing lobby */
    @FXML private VBox joinPanel;

    /** Text field used to enter the game code for joining a lobby */
    @FXML private TextField joinCodeField;

    /** Text field used to enter the nickname for lobby joining */
    @FXML private TextField joinNicknameField;

    /** Label used to display validation or connection errors in join mode */
    @FXML private Label joinErrorLabel;

    /** Stage associated with the login scene */
    private Stage stage;

    /** Server host used for client connections */
    private String host = "localhost";

    /** Communication protocol selected for the client connection. */
    private CommunicationProtocol protocol = CommunicationProtocol.RMI;

    public void setHost(String host) { this.host = host; }
    public void setProtocol(CommunicationProtocol p) { this.protocol = p; }
    public void setStage(Stage stage) { this.stage = stage; }

    //! PANEL NAVIGATION
    /**
     * Shows the panel used to create a new lobby
     */
    @FXML
    private void onCreateSelected() {
        showPanel(createPanel);
    }

    /**
     * Shows the panel used to join an existing lobby
     */
    @FXML
    private void onJoinSelected() {
        showPanel(joinPanel);
    }

    /**
     * Returns to the main menu panel
     */
    @FXML
    private void onBack() {
        showPanel(menuPanel);
    }

    //! ACTIONS
    /**
     * Validates the input fields for lobby creation and, if valid, creates
     * the GUI/client objects needed to connect to the server and create a lobby
     */    @FXML
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
        gui.setConnectionInfo(host, protocol);
        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);

        try {
            hideError(createErrorLabel);
            showInfo(createErrorLabel);
            controller.createLobbyAndConnect(host, numPlayers, protocol);
        } catch (Exception e) {
            showError(createErrorLabel, "Impossible to connect to server");
        }
    }

    /**
     * Validates the input fields for lobby joining and, if valid, creates
     * the GUI/client objects needed to connect to the server and join a lobby
     */
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
        gui.setConnectionInfo(host, protocol);
        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);
        gui.setLobbyRetryEnabled(protocol == CommunicationProtocol.SOCKET);

        try {
            hideError(joinErrorLabel);
            showInfo(joinErrorLabel);
            controller.joinLobbyAndConnect(host, code, protocol);
        } catch (Exception e) {
            showError(joinErrorLabel, "Invalid game code or server unreachable");
        }
    }

    /**
     * Switches to the join panel and shows the provided error message
     *
     * @param error the error message to display
     */
    public void showJoinWithError(String error) {
        showPanel(joinPanel);
        showError(joinErrorLabel, error);
    }


    //! PRIVATE HELPERS
    /**
     * Shows the given panel and hides all the other main login panels
     *
     * @param panel the panel to make visible
     */
    private void showPanel(VBox panel) {
        for (VBox p : new VBox[]{menuPanel, createPanel, joinPanel}) {
            p.setVisible(p == panel);
            p.setManaged(p == panel);
        }
    }

    /**
     * Displays an error message in the given label using error styling
     *
     * @param label the label where the message must be shown
     * @param message the error message to display
     */
    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #cc4444; -fx-font-size: 11px;");
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Displays an info message in the given label using neutral styling
     *
     * @param label the label where the message must be shown
     */
    private void showInfo(Label label) {
        label.setText("Connecting...");
        label.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");
        label.setVisible(true);
        label.setManaged(true);
    }


    /**
     * Hides the given feedback label from the layout
     *
     * @param label the label to hide
     */
    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}