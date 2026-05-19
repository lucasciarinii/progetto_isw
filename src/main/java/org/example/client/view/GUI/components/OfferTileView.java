package org.example.client.view.GUI.components;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.server.model.enums.OfferEffect;

import java.io.InputStream;

/**
 * JavaFX component that represents a tile on the offer track.

 * Shows:
 *  - image (which already contains the visual effect)
 *  - if occupied → colored border with player's totem color and initials
 *  - if free and during PLACE_TOTEMS → yellow dashed border and clickable
 */

public class OfferTileView extends StackPane {

    public static final double TILE_WIDTH  = 120;
    public static final double TILE_HEIGHT = 180;

    private static final String IMAGE_BASE_PATH = "/images/offerTrack/";

    private final OfferTileSnapshot snapshot;
    private final int position;

    @FXML final ImageView imageView;
    @FXML final VBox totemBox;
    @FXML final Label totemLabel;
    @FXML final Label posLabel;

    private Runnable onClickCallback = null;

    public OfferTileView(OfferTileSnapshot snapshot, int position) {
        this.snapshot = snapshot;
        this.position = position;

        setPrefSize(TILE_WIDTH, TILE_HEIGHT);
        setMaxSize(TILE_WIDTH, TILE_HEIGHT);
        setMinSize(TILE_WIDTH, TILE_HEIGHT);
        setStyle(baseStyle() + borderNormal());

        // Image
        imageView = new ImageView();
        imageView.setFitWidth(TILE_WIDTH);
        imageView.setFitHeight(TILE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadTileImage(snapshot.getOfferEffect()));

        // Label position (1-based)
        posLabel = new Label(String.valueOf(position));
        posLabel.setStyle(
                "-fx-font-size: 10px; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: rgba(0,0,0,0.5); " +
                        "-fx-padding: 2 4 2 4; " +
                        "-fx-background-radius: 3;"
        );
        StackPane.setAlignment(posLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(posLabel, new Insets(5, 5, 0, 0));

        // Totem box
        totemBox = new VBox();
        totemBox.setPrefSize(64, 42);
        totemBox.setMaxSize(64, 42);
        totemBox.setMinSize(64, 42);
        totemBox.setAlignment(Pos.CENTER);

        totemLabel = new Label();
        totemLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );
        totemBox.getChildren().add(totemLabel);
        StackPane.setAlignment(totemBox, Pos.TOP_CENTER);
        StackPane.setMargin(totemBox, new Insets(42, 0, 0, 0));

        // Assembly
        getChildren().addAll(imageView, posLabel, totemBox);

        // Initial render
        render();
    }

    /**
     * Makes the tile clickable during the PLACE_TOTEMS phase.
     * Only has an effect if the tile is free.
     */
    public void setSelectable(boolean selectable, Runnable onClick) {
        if (selectable && snapshot.isFree()) {
            this.onClickCallback = onClick;
            setCursor(javafx.scene.Cursor.HAND);
            setStyle(baseStyle() + borderSelectable());
            setOnMouseEntered(this::handleSelectableMouseEntered);
            setOnMouseExited(this::handleSelectableMouseExited);
            setOnMouseClicked(this::handleSelectableMouseClicked);
        } else {
            onClickCallback = null;
            setOnMouseClicked(null);
            setOnMouseEntered(null);
            setOnMouseExited(null);
            setCursor(javafx.scene.Cursor.DEFAULT);
            render();
        }
    }

    public OfferTileSnapshot getSnapshot() { return snapshot; }
    public int getPosition()               { return position; }

    // Rendering
    private void render() {
        if (snapshot.isFree()) {
            setStyle(baseStyle() + borderNormal());
            totemBox.setVisible(false);
        } else {
            String nick = snapshot.getOccupantNickname();
            String hex  = PlayerColorRegistry.getInstance().getHex(nick);

            // Colored border with the player's totem color
            setStyle(baseStyle() +
                    "-fx-border-color: " + hex + "; " +
                    "-fx-border-width: 2.5; " +
                    "-fx-border-radius: 6;"
            );

            // Totem box colored with player's initials
            totemBox.setStyle(
                    "-fx-background-color: " + hex + "; " +
                            "-fx-background-radius: 4;"
            );
            totemLabel.setText(getInitials(nick));
            totemBox.setVisible(true);
        }
    }

    /**
     * Loads the tile image based on the offer effect.
     */
    private Image loadTileImage(OfferEffect effect) {
        String path = IMAGE_BASE_PATH + effect.name() + ".jpg";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[OfferTileView] Image not found: " + path);
                return null;
            }
            return new Image(is);
        } catch (Exception e) {
            System.err.println("[OfferTileView] Error during loading: " + path);
            return null;
        }
    }

    private String getInitials(String nickname) {
        if (nickname == null || nickname.isBlank()) return "?";
        String[] parts = nickname.trim().split("\\s+");
        if (parts.length == 1) {
            return nickname.substring(0, Math.min(2, nickname.length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    private void handleSelectableMouseEntered(MouseEvent event) {
        if (event != null) {
            setStyle(baseStyle() + borderSelectable() + "-fx-background-color: #2e2e1a;");
        }
    }

    private void handleSelectableMouseExited(MouseEvent event) {
        if (event != null) {
            setStyle(baseStyle() + borderSelectable());
        }
    }

    private void handleSelectableMouseClicked(MouseEvent event) {
        if (event != null && onClickCallback != null) {
            onClickCallback.run();
        }
    }

    private String baseStyle() {
        return "-fx-background-color: #1e1e14; " +
                "-fx-background-radius: 6; ";
    }

    private String borderNormal() {
        return "-fx-border-color: #555544; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 6;";
    }

    private String borderSelectable() {
        return "-fx-border-color: #ffcc00; " +
                "-fx-border-width: 2.5; " +
                "-fx-border-style: dashed; " +
                "-fx-border-radius: 6;";
    }
}