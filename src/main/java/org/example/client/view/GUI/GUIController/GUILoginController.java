package org.example.client.view.GUI.GUIController;

import com.mysql.cj.xdevapi.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIHandler;
import javafx.scene.paint.Color;
import org.example.client.view.GUI.registry.CardImageRegistry;

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
    public void onConnect() {
        String nickname = nicknameField.getText().trim();
        String numPlayersText = numPlayersField.getText().trim();

        // 1) Check inputs
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

        // 2) GUIHandler and ClientController creation
        GUIHandler gui = new GUIHandler();
        gui.setPrimaryStage(stage);

        ClientController controller = new ClientController(nickname, gui);
        gui.setController(controller);


        // 3) Connection to server
        try {
            errorLabel.setTextFill(Color.BLACK);
            errorLabel.setText("Connecting...");
            controller.connect(host,  numPlayers);
        } catch (Exception e) {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Impossible to connect to server");
        }


    }

}
