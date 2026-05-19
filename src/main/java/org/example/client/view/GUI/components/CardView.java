package org.example.client.view.GUI.components;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.CardImageRegistry;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * JavaFX component that visually represents a card on the board.

 * Possible states:
 *  NORMAL → card visible, not interactable
 *  SELECTABLE → card highlighted, clickable
 *  SELECTED → card selected (green border)
 *  DISABLED → card grayed out, not clickable
 */

public class CardView extends StackPane {

    // State ──────────────────────────────────────────────────────────────────
    public enum State { NORMAL, SELECTABLE, SELECTED, DISABLED }

    private boolean selected = false;
    private Runnable onClickCallback = null;

    // ── Model ────────────────────────────────────────────────────────────────
    private final Card card;

    // ── JavaFX elements ───────────────────────────────────────────────────────────
    @FXML private final ImageView imageView;
    @FXML private final Label costLabel; // only for BuildingCard
    @FXML private final Label eraLabel;
    @FXML private final VBox disabledOverlay; // gray overlay for DISABLED state
    @FXML private final VBox hoverOverlay; // green overlay for SELECTABLE state

    private ScaleTransition hoverTransition;

    // Border colors per state
    private static final String BORDER_NORMAL = "-fx-border-color: #555544; -fx-border-width: 1.5;";
    private static final String BORDER_SELECTABLE = "-fx-border-color: #ffcc00; -fx-border-width: 2.5;";
    private static final String BORDER_SELECTED = "-fx-border-color: #00cc66; -fx-border-width: 3;";
    private static final String BORDER_DISABLED = "-fx-border-color: #333322; -fx-border-width: 1;";

    public CardView(Card card, double width, double height) {
        this.card = card;

        setPrefSize(width, height);
        setMaxSize(width, height);
        setMinSize(width, height);
        setStyle("-fx-background-color: #2a2a1e; -fx-border-radius: 6; -fx-background-radius: 6; " + BORDER_NORMAL);

        // Card image
        imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        // Load the image from the registry using the card's ID
        imageView.setImage(CardImageRegistry.getInstance().getImage(card.getId()));

        // Era label (top-left corner)
        eraLabel = new Label(card.getEra().toString());
        eraLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.55); " +
                        "-fx-text-fill: #f0e0b0; " +
                        "-fx-font-size: 9px; " +
                        "-fx-padding: 1 4 1 4; " +
                        "-fx-background-radius: 3;"
        );
        StackPane.setAlignment(eraLabel, Pos.TOP_LEFT);

        // Label cost (bottom-left) — only BuildingCard
        costLabel = new Label();
        if (card.isBuilding()) {
            BuildingCard bc = (BuildingCard) card;
            costLabel.setText("Food: " + bc.getFoodCost());
            costLabel.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.65); " +
                            "-fx-text-fill: #ffcc88; " +
                            "-fx-font-size: 10px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 2 5 2 5; " +
                            "-fx-background-radius: 3;"
            );
        }
        costLabel.setVisible(card.isBuilding());
        StackPane.setAlignment(costLabel, Pos.BOTTOM_LEFT);

        // Gray overlay (DISABLED)
        disabledOverlay = new VBox();
        disabledOverlay.setPrefSize(width, height);
        disabledOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 6;");
        disabledOverlay.setVisible(false);
        disabledOverlay.setMouseTransparent(true); // does not block clicks (they are blocked by state)

        // Green overlay (SELECTABLE hover)
        hoverOverlay = new VBox();
        hoverOverlay.setPrefSize(width, height);
        hoverOverlay.setStyle("-fx-background-color: rgba(0, 204, 102, 0.28); -fx-background-radius: 6;");
        hoverOverlay.setVisible(false);
        hoverOverlay.setMouseTransparent(true);

        // StackPane assembly (image, overlay, labels)
        getChildren().addAll(imageView, disabledOverlay, hoverOverlay, eraLabel, costLabel);
    }

    /**
     * Sets the visual state of the card, including border, overlay, and cursor.
     */
    public void setState(State state) {
        this.selected = (state == State.SELECTED);

        // Reset effects before handling new state
        imageView.setEffect(null);
        disabledOverlay.setVisible(false);
        hoverOverlay.setVisible(false);
        setOnMouseClicked(null);
        setOnMouseEntered(null);
        setOnMouseExited(null);
        stopHoverAnimation();
        setScaleX(1.0);
        setScaleY(1.0);
        setStyle(getBaseStyle());
        switch (state) {
            case NORMAL -> {
                setStyle(getBaseStyle() + BORDER_NORMAL);
                setCursor(javafx.scene.Cursor.DEFAULT);
            }
            case SELECTABLE -> {
                setStyle(getBaseStyle() + BORDER_SELECTABLE);
                setCursor(javafx.scene.Cursor.HAND);
                setOnMouseEntered(e -> {
                    hoverOverlay.setVisible(true);
                    imageView.setEffect(brighten(0.15));
                    playHoverAnimation(1.04);
                });
                setOnMouseExited(e  -> {
                    hoverOverlay.setVisible(false);
                    imageView.setEffect(null);
                    playHoverAnimation(1.0);
                });
            }
            case SELECTED -> {
                setStyle(getBaseStyle() + BORDER_SELECTED);
                setCursor(javafx.scene.Cursor.HAND);
                imageView.setEffect(brighten(0.08));
            }
            case DISABLED -> {
                setStyle(getBaseStyle() + BORDER_DISABLED);
                setCursor(javafx.scene.Cursor.DEFAULT);
                disabledOverlay.setVisible(true); // show dark overlay
                // de-saturate image
                ColorAdjust ca = new ColorAdjust();
                ca.setSaturation(-0.7);
                ca.setBrightness(-0.2);
                imageView.setEffect(ca);
            }
        }
    }

    /**
     * Makes the card selectable and registers a click callback.
     */
    public void setSelectable(boolean selectable, Runnable onClick) {
        if (selectable) {
            setState(State.SELECTABLE);
            this.onClickCallback = onClick;
            setOnMouseClicked(e -> {
                if (onClickCallback != null) onClickCallback.run();
            });
        } else {
            setState(State.NORMAL);
            this.onClickCallback = null;
        }
    }

    /**
     * Toggles the selected state of the card.
     * Called by GUIGameController when the user clicks a selectable card.
     */
    public void toggleSelected() {
        selected = !selected;
        setState(selected ? State.SELECTED : State.SELECTABLE);
        // Resets the click handler after setState (which resets it)
        setOnMouseClicked(e -> {
            if (onClickCallback != null) onClickCallback.run();
        });
    }

    public boolean isSelected() {
        return selected;
    }

    public Card getCard() {
        return card;
    }


    private String getBaseStyle() {
        return "-fx-background-color: #2a2a1e; -fx-border-radius: 6; -fx-background-radius: 6; ";
    }

    private ColorAdjust brighten(double amount) {
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(amount);
        return ca;
    }

    private void playHoverAnimation(double targetScale) {
        stopHoverAnimation();
        hoverTransition = new ScaleTransition(Duration.millis(120), this);
        hoverTransition.setToX(targetScale);
        hoverTransition.setToY(targetScale);
        hoverTransition.play();
    }

    private void stopHoverAnimation() {
        if (hoverTransition != null) {
            hoverTransition.stop();
            hoverTransition = null;
        }
    }
}