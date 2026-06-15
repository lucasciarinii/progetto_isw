package org.example.client.view.GUI.components;

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
 */

public class CardView extends StackPane {

    /**
     * Visual state of the card component.
     * NORMAL → card visible, not interactable
     * SELECTABLE → card highlighted, clickable
     * SELECTED → card selected (green border)
     * DISABLED → card grayed out, not clickable
     */
    public enum State { NORMAL, SELECTABLE, SELECTED, DISABLED }

    private boolean selected = false;
    private Runnable onClickCallback = null;

    // Model
    private final Card card;

    // Elements
    private final ImageView imageView;
    private final VBox disabledOverlay; // gray overlay for DISABLED state
    private final VBox hoverOverlay; // green overlay for SELECTABLE state

    private ScaleTransition hoverTransition;

    // Border colors per state
    private static final String BORDER_NORMAL = "-fx-border-color: #555544; -fx-border-width: 1.5;";
    private static final String BORDER_SELECTABLE = "-fx-border-color: #ffcc00; -fx-border-width: 2.5;";
    private static final String BORDER_SELECTED = "-fx-border-color: #00cc66; -fx-border-width: 3.5; -fx-border-style: solid; -fx-border-insets: -0.5;";
    private static final String BORDER_DISABLED = "-fx-border-color: #333322; -fx-border-width: 1;";

    private static final double HOVER_OVERLAY_OPACITY = 0.28;
    private static final double SELECTED_OVERLAY_OPACITY = 0.38;

    /**
     * Constructs a visual card component for the given model card and dimensions.
     *
     * @param card the card model represented by this view
     * @param width the preferred width of the rendered card
     * @param height the preferred height of the rendered card
     */
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

        // Labels
        Label eraLabel = new Label(card.getEra().toString());
        Label costLabel = new Label();

        // Era label (top-left corner)
        eraLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.55); " +
                        "-fx-text-fill: #f0e0b0; " +
                        "-fx-font-size: 9px; " +
                        "-fx-padding: 1 4 1 4; " +
                        "-fx-background-radius: 3;"
        );
        StackPane.setAlignment(eraLabel, Pos.TOP_LEFT);

        // Label cost (bottom-left) — only BuildingCard
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
        disabledOverlay.setMinSize(width, height);
        disabledOverlay.setMaxSize(width, height);
        disabledOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 6;");
        disabledOverlay.setVisible(false);
        disabledOverlay.setMouseTransparent(true); // does not block clicks (they are blocked by state)

        // Green overlay (SELECTABLE hover)
        hoverOverlay = new VBox();
        hoverOverlay.setPrefSize(width, height);
        hoverOverlay.setMinSize(width, height);
        hoverOverlay.setMaxSize(width, height);
        hoverOverlay.setStyle("-fx-background-color: rgba(0, 204, 102, 1); -fx-background-radius: 6;");
        hoverOverlay.setOpacity(HOVER_OVERLAY_OPACITY);
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
                setOnMouseEntered(_ -> {
                    hoverOverlay.setOpacity(HOVER_OVERLAY_OPACITY);
                    hoverOverlay.setVisible(true);
                    imageView.setEffect(brighten(0.15));
                    playHoverAnimation(1.04);
                });
                setOnMouseExited(_  -> {
                    hoverOverlay.setVisible(false);
                    imageView.setEffect(null);
                    playHoverAnimation(1.0);
                });
            }
            case SELECTED -> {
                setStyle(getBaseStyle() + BORDER_SELECTED);
                setCursor(javafx.scene.Cursor.HAND);
                hoverOverlay.setOpacity(SELECTED_OVERLAY_OPACITY);
                hoverOverlay.setVisible(true);
                imageView.setEffect(brighten(0.15));
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
     * Enables or disables selectable behavior for the card and registers
     * the callback to invoke when the card is clicked
     *
     * @param selectable {@code true} to make the card selectable, {@code false} otherwise
     * @param onClick the callback to invoke on click when selectable
     */
    public void setSelectable(boolean selectable, Runnable onClick) {
        if (selectable) {
            setState(State.SELECTABLE);
            this.onClickCallback = onClick;
            setOnMouseClicked(_ -> {
                if (onClickCallback != null) onClickCallback.run();
            });
        } else {
            setState(State.NORMAL);
            this.onClickCallback = null;
        }
    }

    /**
     * Toggles the selected state of the card
     * Called by GUIGameController when the user clicks a selectable card
     */
    public void toggleSelected() {
        selected = !selected;
        setState(selected ? State.SELECTED : State.SELECTABLE);
        // Resets the click handler after setState (which resets it)
        setOnMouseClicked(_ -> {
            if (onClickCallback != null) onClickCallback.run();
        });
    }

    /**
     * Returns whether the card is currently marked as selected
     *
     * @return {@code true} if the card is selected, {@code false} otherwise
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the model card represented by this component
     *
     * @return the underlying card
     */
    public Card getCard() {
        return card;
    }

    /**
     * Returns the common base CSS style applied to the card container
     *
     * @return the base style string
     */
    private String getBaseStyle() {
        return "-fx-background-color: #2a2a1e; -fx-border-radius: 6; -fx-background-radius: 6; ";
    }

    /**
     * Creates a brightness adjustment effect for the card image.
     *
     * @param amount the brightness delta to apply
     * @return the configured {@link ColorAdjust} effect
     */
    @SuppressWarnings("SameParameterValue")
    private ColorAdjust brighten(double amount) {
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(amount);
        return ca;
    }

    /**
     * Plays the hover scale animation toward the given target scale.
     *
     * @param targetScale the target scale factor for the card view
     */
    private void playHoverAnimation(double targetScale) {
        stopHoverAnimation();
        hoverTransition = new ScaleTransition(Duration.millis(120), this);
        hoverTransition.setToX(targetScale);
        hoverTransition.setToY(targetScale);
        hoverTransition.play();
    }

    /**
     * Stops the currently running hover animation, if present.
     */
    private void stopHoverAnimation() {
        if (hoverTransition != null) {
            hoverTransition.stop();
            hoverTransition = null;
        }
    }
}