package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIHandler;
import org.example.network.CommunicationProtocol;

public class GUILoginController {

    @FXML private TextField nicknameField;
    @FXML private TextField numPlayersField;
    @FXML private Label errorLabel;

    private Stage stage;
    private String host = "localhost";
    private int port = 1099;
    private CommunicationProtocol protocol = CommunicationProtocol.RMI;

    public void setHost(String host) {
        this.host = host;
    }

    public void setProtocol(CommunicationProtocol protocol) {
        this.protocol = protocol;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onConnect() {
        String nickname = nicknameField.getText().trim();
        String numPlayersText = numPlayersField.getText().trim();

        // 1) Check inputs
        if (nickname.isEmpty()) {
            showError("Nickname cannot be empty");
            return;
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(numPlayersText);
        } catch (NumberFormatException e) {
            showError("Invalid number format");
            return;
        }

        if ( numPlayers < 2 || numPlayers > 5) {
            showError("Invalid number of players (2-5)");
            return;
        }

        // 2) GUIHandler and ClientController creation
        GUIHandler gui = new GUIHandler();
        gui.setPrimaryStage(stage);

        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);


        // 3) Connection to server
        try {
            errorLabel.setVisible(false);  // nascondi eventuali errori precedenti
            errorLabel.setText("Connecting...");
            errorLabel.setStyle("-fx-text-fill: #888866; -fx-font-size: 11px;");
            errorLabel.setVisible(true);
            controller.createLobbyAndConnect(host, port, numPlayers, protocol);
        } catch (Exception e) {
            errorLabel.setTextFill(Color.RED);
            showError("Impossible to connect to server");
        }


    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

}
