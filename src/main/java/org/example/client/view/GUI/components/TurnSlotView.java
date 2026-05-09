package org.example.client.view.GUI.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.Snapshots.TurnSlotSnapshot;

import java.io.InputStream;
import java.util.List;

/**
 * JavaFX component that represents the turn order tile.

 * It's a SINGLE tile that contains all the slots (one per player).
 * The background image is ts_N_players.jpg (e.g. ts_2_players.jpg).
 * On top of the image, we overlay the colored totems of the players
 * in the position corresponding to their slot.

 * Positions Y of the totems are hardcoded per slot.
 */

public class TurnSlotView extends StackPane {

    private static final String IMAGE_BASE_PATH = "/images/turnOrderTile/";

    // TODO: tutte le dimensioni dobbiamo sistemarle ad occhio ----------------------------
    // Tile dimensions — same proportion as the image
    public static final double TILE_WIDTH = 120;
    public static final double TILE_HEIGHT = 180;

    // Position X of the center of the totem inside each slot (to be adjusted by eye)
    private static final double TOTEM_X = 38;

    // Dimensions of the totem box — adapted to the rectangles in the image
    private static final double TOTEM_W = 60;
    private static final double TOTEM_H = 38;

    private final List<TurnSlotSnapshot> slots;

    // Position Y of the first slot (from the top of the tile) and distance between slots
    private final double FIRST_SLOT_Y;   // px from top to center of first slot
    private final double SLOT_SPACING;   // px between center of one slot and the next

    // Pane above the image where we position the totems with absolute coordinates
    private final Pane totemLayer;

    public TurnSlotView(List<TurnSlotSnapshot> slots) {
        this.slots = slots;
        int numPlayers = slots.size();

        this.FIRST_SLOT_Y = switch (numPlayers) {
            case 2 -> 50;
            case 3 -> 40;
            case 4 -> 27;
            case 5 -> 11;
            default -> 22;
        };
        this.SLOT_SPACING = switch (numPlayers) {
            case 2, 3 -> 40;
            case 4, 5 -> 41;
            default -> 40;
        };

        setPrefSize(TILE_WIDTH, TILE_HEIGHT);
        setMaxSize(TILE_WIDTH, TILE_HEIGHT);
        setMinSize(TILE_WIDTH, TILE_HEIGHT);

        // Background image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(TILE_WIDTH);
        imageView.setFitHeight(TILE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadImage(numPlayers));

        // Transparent layer for totems (absolute coordinates)
        totemLayer = new Pane();
        totemLayer.setPrefSize(TILE_WIDTH, TILE_HEIGHT);
        totemLayer.setMouseTransparent(true);

        // Assembly
        getChildren().addAll(imageView, totemLayer);

        // Render
        render();
    }

    // Rendering

    /*
    Redraws the totems based on the current state of the slots.
    Should be called every time a new GameStateUpdateMessage arrives.
    */
    public void render() {
        totemLayer.getChildren().clear();

        for (int i = 0; i < slots.size(); i++) {
            TurnSlotSnapshot slot = slots.get(i);
            if (!slot.isFree()) {
                VBox totemBox = buildTotemBox(slot.getOccupantNickname());

                // Absolute position of the totem inside the Pane
                double y = FIRST_SLOT_Y + i * SLOT_SPACING;

                totemBox.setLayoutX(TOTEM_X);
                totemBox.setLayoutY(y);
                totemLayer.getChildren().add(totemBox);
            }
        }
    }

    //! UTILITY private ────────────────────────────────────────────────────────

    // Builds the colored box of the totem with the player's initials.
    private VBox buildTotemBox(String nickname) {
        String hex = PlayerColorRegistry.getInstance().getHex(nickname);

        Label label = new Label(getInitials(nickname));
        label.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        VBox box = new VBox(label);
        box.setPrefSize(TOTEM_W, TOTEM_H);
        box.setMaxSize(TOTEM_W, TOTEM_H);
        box.setMinSize(TOTEM_W, TOTEM_H);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: " + hex + "; " +
                        "-fx-background-radius: 4; " +
                        "-fx-opacity: 0.85;"
        );
        return box;
    }

    private Image loadImage(int numPlayers) {
        String path = IMAGE_BASE_PATH + "ts_" + numPlayers + "_players.jpg";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[TurnSlotView] Image not found: " + path);
                return null;
            }
            return new Image(is);
        } catch (Exception e) {
            System.err.println("[TurnSlotView] Error during loading: " + path);
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
}