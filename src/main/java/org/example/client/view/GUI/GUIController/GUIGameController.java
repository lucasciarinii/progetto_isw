package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.network.GameStateUpdateMessage;

public class GUIGameController {



    // ROUND infos
    @FXML private Label currentPlayer;
    @FXML private Label currentRound;
    @FXML private Label currentEra;
    @FXML private Label currentPhase;

    // PLAYERS
    @FXML

    public void update(GameStateUpdateMessage update) {

        currentPlayer.setText(update.getCurrentPlayerNickname());
        currentRound.setText(Integer.toString(update.getCurrentRound()));
        currentEra.setText(update.getCurrentEra().toString());
        currentPhase.setText(update.getCurrentPhase().toString());

    }
}
