package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.network.messages.LobbyUpdateMessage;

/**
 * Controller for the lobby screen.
 */
public class GUILobbyController {

    @FXML private Label playersCountLabel;
    @FXML private ListView<String> playersList;
    @FXML private Label statusLabel;

    /**
     * Updates the lobby view with the latest status.
     *
     * @param update the lobby update message
     */
    public void update(LobbyUpdateMessage update) {
        // Update the number of players in the lobby.
        playersCountLabel.setText(update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players connected");

        // Display the connected players.
        playersList.getItems().setAll(update.getPlayerNicknames());

        // Display lobby status.
        if ( update.isGameStarting() ) {
            statusLabel.setText("Game is starting...");
        } else {
            statusLabel.setText("Waiting for players...");
        }
    }

}
