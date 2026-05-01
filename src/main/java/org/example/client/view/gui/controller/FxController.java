package org.example.client.view.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.client.ClientController;

public class FxController {

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
            ClientController client = new ClientController(nickname);
            client.connect(host, numPlayers);
        } catch (Exception e) {
            throw new Exception("Impossible to connect to server");
        }


    }

}
