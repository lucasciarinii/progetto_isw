package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.client.ClientController;
import org.example.client.view.GUI.GUIHandler;

public class GUIController {

    @FXML public TextField nicknameField;
    @FXML public TextField numPlayersField;
    private String host = "localhost";

    public void setHost(String host) {
        this.host = host;
    }

    @FXML
    public void onConnect() throws Exception {
        String nickname = nicknameField.getText();
        String numPlayersText = numPlayersField.getText();

        if (nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be empty");
        }

        int numPlayers;
        try {
            numPlayers = Integer.parseInt(numPlayersText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number of players");
        }

        try {
            GUIHandler gui = new GUIHandler();
            ClientController clientController = new ClientController(nickname, gui);
            gui.setController(clientController);
            clientController.connect(host, numPlayers);
            //TODO: update scene after client connection to show game
        } catch (Exception e) {
            throw new Exception("Impossible to connect to server");
        }


    }

}
