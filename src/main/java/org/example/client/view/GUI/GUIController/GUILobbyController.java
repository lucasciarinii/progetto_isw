package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.network.LobbyUpdateMessage;

public class GUILobbyController {

    @FXML private Label playersCountLabel;
    @FXML private ListView<String> playersList;
    @FXML private Label statusLabel;

    public void update(LobbyUpdateMessage update) {
        // Update the number of players in lobby
        playersCountLabel.setText(update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players connected");

        // display the name of the players in lobby
        playersList.getItems().setAll(update.getPlayerNicknames());

        // display lobby status
        if ( update.isGameStarting() ) {
            statusLabel.setText("Game is starting...");
        } else {
            statusLabel.setText("Waiting for players...");
        }
    }

}
