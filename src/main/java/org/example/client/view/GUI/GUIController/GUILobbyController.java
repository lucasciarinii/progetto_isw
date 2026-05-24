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

public class GUILobbyController {

    // ── UI bindings ─────────────────────────────────────────────────────────────
    @FXML private Label gameCodeLabel;
    @FXML private Label playersCountLabel;
    @FXML private ListView<String> playersList;
    @FXML private Label statusLabel;

    // ── Setup mouse interactions on the game code label ─────────────────────────
    @FXML
    private void initialize() {
        gameCodeLabel.setStyle(gameCodeLabel.getStyle() + "; -fx-cursor: hand;");

        gameCodeLabel.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), gameCodeLabel);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        gameCodeLabel.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), gameCodeLabel);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        gameCodeLabel.setOnMouseClicked(this::copyGameCode);
    }

    // ── Render lobby state from server updates ──────────────────────────────────
    public void update(LobbyUpdateMessage update) {
        gameCodeLabel.setText(update.getGameID());
        playersCountLabel.setText(update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players connected");
        playersList.getItems().setAll(update.getPlayerNicknames());
        statusLabel.setText(update.isGameStarting() ? "Game is starting..." : "Waiting for players...");
    }

    // ── Copy game code to clipboard with quick visual feedback ──────────────────
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
        st.setOnFinished(e -> gameCodeLabel.setText(oldText));
        st.play();
    }
}