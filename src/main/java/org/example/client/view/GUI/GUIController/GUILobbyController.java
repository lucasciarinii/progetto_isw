package org.example.client.view.GUI.GUIController;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.example.network.messages.LobbyUpdateMessage;

/**
 * Controller for the lobby screen
 */
public class GUILobbyController {

    /** Label displaying the current game code */
    @FXML private Label gameCodeLabel;

    /** Label displaying the number of connected players over the required amount */
    @FXML private Label playersCountLabel;

    /** List view showing the nicknames of the players currently in the lobby */
    @FXML private ListView<String> playersList;

    /** Label displaying the current lobby status message */
    @FXML private Label statusLabel;

    /**
     * Initializes the lobby view after FXML injection, enabling mouse interactions
     * on the game-code label for hover feedback and clipboard copying
     */
    @FXML
    private void initialize() {
        gameCodeLabel.setStyle(gameCodeLabel.getStyle() + "; -fx-cursor: hand;");

        gameCodeLabel.setOnMouseEntered(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), gameCodeLabel);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        gameCodeLabel.setOnMouseExited(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), gameCodeLabel);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        gameCodeLabel.setOnMouseClicked(this::copyGameCode);
    }

    /**
     * Updates the lobby view with the latest status.
     *
     * @param update the lobby update message
     */
    public void update(LobbyUpdateMessage update) {
        gameCodeLabel.setText(update.getGameID());
        playersCountLabel.setText(update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players connected");
        playersList.getItems().setAll(update.getPlayerNicknames());
        statusLabel.setText(update.isGameStarting() ? "Game is starting..." : "Waiting for players...");
    }

    /**
     * Copies the currently displayed game code to the system clipboard and
     * provides brief visual feedback on the label
     *
     * @param event the mouse-click event that triggered the copy action
     */
    private void copyGameCode(MouseEvent event) {
        String code = gameCodeLabel.getText();
        if (code == null || code.isBlank() || "———".equals(code)) return;

        ClipboardContent content = new ClipboardContent();
        content.putString(code);
        Clipboard.getSystemClipboard().setContent(content);

        String oldText = gameCodeLabel.getText();
        gameCodeLabel.setText(code + " ✓");

        ScaleTransition st = new ScaleTransition(Duration.millis(120), gameCodeLabel);
        st.setToX(1.08);
        st.setToY(1.08);
        st.setOnFinished(_ -> gameCodeLabel.setText(oldText));
        st.play();
    }
}