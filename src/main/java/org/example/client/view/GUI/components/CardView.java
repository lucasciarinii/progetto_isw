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
    @FXML private final Label costLabel;   // mostrato solo per BuildingCard
    @FXML private final Label eraLabel;    // era della carta (I, II, III)
    @FXML private final VBox overlay;     // overlay scuro per stato DISABLED

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
        overlay = new VBox();
        overlay.setPrefSize(width, height);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 6;");
        overlay.setVisible(false);
        overlay.setMouseTransparent(true); // does not block clicks (they are blocked by state)

        // StackPane assembly (image, overlay, labels)
        getChildren().addAll(imageView, overlay, eraLabel, costLabel);
    }

    /**
     * Sets the visual state of the card, including border, overlay, and cursor.
     */
    public void setState(State state) {
        this.selected = (state == State.SELECTED);

        // Reset effects before handling new state
        imageView.setEffect(null);
        overlay.setVisible(false);
        setOnMouseClicked(null);
        setStyle(getBaseStyle());
        switch (state) {
            case NORMAL -> {
                setStyle(getBaseStyle() + BORDER_NORMAL);
                setCursor(javafx.scene.Cursor.DEFAULT);
            }
            case SELECTABLE -> {
                setStyle(getBaseStyle() + BORDER_SELECTABLE);
                setCursor(javafx.scene.Cursor.HAND);
                // brighten to hover
                setOnMouseEntered(e -> imageView.setEffect(brighten(0.15)));
                setOnMouseExited(e  -> imageView.setEffect(null));
            }
            case SELECTED -> {
                setStyle(getBaseStyle() + BORDER_SELECTED);
                setCursor(javafx.scene.Cursor.HAND);
                imageView.setEffect(brighten(0.08));
            }
            case DISABLED -> {
                setStyle(getBaseStyle() + BORDER_DISABLED);
                setCursor(javafx.scene.Cursor.DEFAULT);
                overlay.setVisible(true); // show dark overlay
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
}