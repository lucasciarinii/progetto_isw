package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIHandler;
import javafx.scene.paint.Color;

import java.awt.*;

public class GUILoginController {

    @FXML private TextField nicknameField;
    @FXML private TextField numPlayersField;
    @FXML private Label errorLabel;

    private Stage stage;
    private String host = "localhost";

    public void setHost(String host) {
        this.host = host;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onConnect() throws Exception {
        String nickname = nicknameField.getText();
        String numPlayersText = numPlayersField.getText();

        if (nickname.isEmpty()) {
            errorLabel.setText("Nickname cannot be empty");
            return;
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(numPlayersText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid number format");
            return;
        }

        if ( numPlayers < 2 || numPlayers > 5) {
            errorLabel.setText("Invalid number of players (2-5)");
            return;
        }

        // Connection
        try {
            GUIHandler gui = new GUIHandler();
            ClientController clientController = new ClientController(nickname, gui);
            gui.setController(clientController);
            clientController.connect(host, numPlayers);
            errorLabel.setTextFill(Color.BLACK);
            errorLabel.setText("Connecting...");
        } catch (Exception e) {
            throw new Exception("Impossible to connect to server");
        }


    }

}
