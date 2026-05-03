package org.example.client.view.GUI.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.CardImageRegistry;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX component that represents a single player's panel.

 * Shows:
 *  - Nickname with player's totem color
 *  - Points and food
 *  - Building discount (if > 0)
 *  - All owned cards as scrollable thumbnails

 * The local player's panel has a thicker, colored border.
 * Other players have a thin border in their color.
 */
public class PlayerPanelView extends VBox {

    // Fixed panel width — the sidebar has a fixed width
    public static final double PANEL_WIDTH = 180;

    // Card thumbnail dimensions inside the panel
    private static final double CARD_W = 46;
    private static final double CARD_H = 64;

    private final PlayerSnapshot snapshot;
    private final boolean isLocalPlayer;

    // Updatable nodes
    private final Label foodLabel;
    private final Label pointsLabel;
    private final Label discountLabel;
    private final FlowPane cardsPane;

    public PlayerPanelView(PlayerSnapshot snapshot, boolean isLocalPlayer) {
        this.snapshot = snapshot;
        this.isLocalPlayer = isLocalPlayer;

        String hex = PlayerColorRegistry.getInstance().getHex(snapshot.getNickname());

        // Panel style
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setSpacing(6);
        setPadding(new Insets(8));

        if (isLocalPlayer) {
            // Thick colored border for the local player
            setStyle(
                    "-fx-background-color: #1e1e14; " +
                            "-fx-border-color: " + hex + "; " +
                            "-fx-border-width: 3; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8;"
            );
        } else {
            // Thin colored border for other players
            setStyle(
                    "-fx-background-color: #181810; " +
                            "-fx-border-color: " + hex + "; " +
                            "-fx-border-width: 1.2; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8;"
            );
        }

        // Header: colored dot + nickname
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);

        // Colored dot = player's totem
        VBox dot = new VBox();
        dot.setPrefSize(12, 12);
        dot.setMinSize(12, 12);
        dot.setMaxSize(12, 12);
        dot.setStyle(
                "-fx-background-color: " + hex + "; " +
                        "-fx-background-radius: 6;"
        );

        Label nicknameLabel = new Label(snapshot.getNickname());
        nicknameLabel.setStyle(
                "-fx-text-fill: " + hex + "; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold;"
        );
        nicknameLabel.setMaxWidth(PANEL_WIDTH - 30);

        header.getChildren().addAll(dot, nicknameLabel);

        // Stats: points and food
        foodLabel     = new Label("Food: " + snapshot.getFood());
        pointsLabel   = new Label("Stars: " + snapshot.getPoints());
        discountLabel = new Label("Building Discount: -" + snapshot.getDiscountOnBuilding());

        String statStyle =
                "-fx-text-fill: #d0c8a0; " +
                        "-fx-font-size: 11px;";

        foodLabel.setStyle(statStyle);
        pointsLabel.setStyle(statStyle);
        discountLabel.setStyle(statStyle);
        discountLabel.setVisible(snapshot.getDiscountOnBuilding() > 0);

        HBox stats = new HBox(10, foodLabel, pointsLabel);
        stats.setAlignment(Pos.CENTER_LEFT);

        // Separator
        Label separator = new Label("─────────────────");
        separator.setStyle("-fx-text-fill: #333322; -fx-font-size: 9px;");

        // Cards: scrollable FlowPane
        cardsPane = new FlowPane();
        cardsPane.setHgap(3);
        cardsPane.setVgap(3);
        cardsPane.setPrefWidth(PANEL_WIDTH - 16);
        cardsPane.setStyle("-fx-background-color: transparent;");

        renderCards();

        ScrollPane scrollPane = new ScrollPane(cardsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);  // height of the scrollable card area
        scrollPane.setStyle(
                "-fx-background: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Assemble
        getChildren().addAll(header, stats, discountLabel, separator, scrollPane);
    }

    //! EXTERNAL methods ─────────────────────────────────────────────────────────────

    /**
     * Updates the panel with new player data.
     * Called by GUIGameController on every GameStateUpdateMessage.
     */
    public void update(PlayerSnapshot newSnapshot) {
        foodLabel.setText("Food: " + newSnapshot.getFood());
        pointsLabel.setText("Stars: " + newSnapshot.getPoints());
        discountLabel.setText("Building Discount: -" + newSnapshot.getDiscountOnBuilding());
        discountLabel.setVisible(newSnapshot.getDiscountOnBuilding() > 0);

        // Rebuilds card thumbnails
        cardsPane.getChildren().clear();
        renderCards(newSnapshot);
    }

    // Rendering ─────────────────────────────────────────────────────────

    /**
     * First render — uses the snapshot passed to the constructor.
     */
    private void renderCards() {
        renderCards(snapshot);
    }

    /**
     * Builds thumbnails for all of the player's cards.
     * Order: characters (by type) → buildings.
     */
    private void renderCards(PlayerSnapshot s) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(s.getOwnedHunters());
        allCards.addAll(s.getOwnedGatherers());
        allCards.addAll(s.getOwnedBuilders());
        allCards.addAll(s.getOwnedShamans());
        allCards.addAll(s.getOwnedArtists());
        allCards.addAll(s.getOwnedInventors());
        allCards.addAll(s.getOwnedBuildings());

        if (allCards.isEmpty()) {
            Label empty = new Label("No cards");
            empty.setStyle("-fx-text-fill: #555544; -fx-font-size: 10px;");
            cardsPane.getChildren().add(empty);
            return;
        }

        for (Card card : allCards) {
            CardView mini = new CardView(card, CARD_W, CARD_H);
            mini.setState(CardView.State.NORMAL);
            cardsPane.getChildren().add(mini);
        }
    }
}