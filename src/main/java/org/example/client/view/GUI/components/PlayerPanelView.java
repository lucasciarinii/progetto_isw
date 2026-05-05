package org.example.client.view.GUI.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;

import java.util.List;

/**
 * JavaFX component representing a single player's panel with cascading card stacks.
 */
public class PlayerPanelView extends VBox {

    public static final double PANEL_WIDTH = 250;
    private static final double CARD_W = 60;
    private static final double CARD_H = 86;

    private final PlayerSnapshot snapshot;

    private final Label pointsLabel;
    private final Label foodLabel;
    private final Label discountLabel;
    private final HBox stacksContainer;

    public PlayerPanelView(PlayerSnapshot snapshot, boolean isLocalPlayer) {
        this.snapshot = snapshot;
        // Removed the unused class field 'isLocalPlayer' to fix the IDE warning.
        // We just use the parameter directly here to set the border.

        String hex = PlayerColorRegistry.getInstance().getHex(snapshot.getNickname());

        setPrefWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setSpacing(6);
        setPadding(new Insets(8));

        // Styling based on local player status
        if (isLocalPlayer) {
            setStyle("-fx-background-color: #1e1e14; -fx-border-color: " + hex + "; -fx-border-width: 3; -fx-border-radius: 8; -fx-background-radius: 8;");
        } else {
            setStyle("-fx-background-color: #181810; -fx-border-color: " + hex + "; -fx-border-width: 1.2; -fx-border-radius: 8; -fx-background-radius: 8;");
        }

        // Header
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox dot = new VBox();
        dot.setPrefSize(12, 12);
        dot.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 6;");
        Label nicknameLabel = new Label(snapshot.getNickname());
        nicknameLabel.setStyle("-fx-text-fill: " + hex + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        header.getChildren().addAll(dot, nicknameLabel);

        // Stats
        pointsLabel = new Label("Points: " + snapshot.getPoints());
        foodLabel = new Label("Food: " + snapshot.getFood());
        discountLabel = new Label("Building Discount: -" + snapshot.getDiscountOnBuilding());

        String statStyle = "-fx-text-fill: #d0c8a0; -fx-font-size: 11px;";
        pointsLabel.setStyle(statStyle);
        foodLabel.setStyle(statStyle);
        discountLabel.setStyle(statStyle);
        discountLabel.setVisible(snapshot.getDiscountOnBuilding() > 0);

        HBox stats = new HBox(10, pointsLabel, foodLabel);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label separator = new Label("─────────────────────────");
        separator.setStyle("-fx-text-fill: #333322; -fx-font-size: 9px;");

        // Cards Container
        stacksContainer = new HBox(5);
        stacksContainer.setAlignment(Pos.TOP_LEFT);

        renderCards(snapshot);

        ScrollPane scrollPane = new ScrollPane(stacksContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().addAll(header, stats, discountLabel, separator, scrollPane);
    }

    public void update(PlayerSnapshot newSnapshot) {
        pointsLabel.setText("Points: " + newSnapshot.getPoints());
        foodLabel.setText("Food: " + newSnapshot.getFood());
        discountLabel.setText("Building Discount: -" + newSnapshot.getDiscountOnBuilding());
        discountLabel.setVisible(newSnapshot.getDiscountOnBuilding() > 0);

        stacksContainer.getChildren().clear();
        renderCards(newSnapshot);
    }

    private void renderCards(PlayerSnapshot s) {
        // Add specific stacks for each card type
        addCardStackIfNotEmpty(s.getOwnedHunters());
        addCardStackIfNotEmpty(s.getOwnedGatherers());
        addCardStackIfNotEmpty(s.getOwnedBuilders());
        addCardStackIfNotEmpty(s.getOwnedShamans());
        addCardStackIfNotEmpty(s.getOwnedArtists());
        addCardStackIfNotEmpty(s.getOwnedInventors());
        addCardStackIfNotEmpty(s.getOwnedBuildings());
    }

    /**
     * Note the use of the wildcard "? extends Card".
     * This allows the method to accept List<Hunter>, List<BuildingCard>, etc.
     */
    private void addCardStackIfNotEmpty(List<? extends Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        VBox stack = new VBox();
        stack.setSpacing(-60); // Negative spacing for cascading effect
        stack.setAlignment(Pos.TOP_CENTER);
        stack.setPadding(new Insets(0, 0, 10, 0));

        for (Card card : cards) {
            CardView mini = new CardView(card, CARD_W, CARD_H);
            mini.setState(CardView.State.NORMAL);
            mini.setStyle(mini.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 2);");
            stack.getChildren().add(mini);
        }

        stacksContainer.getChildren().add(stack);
    }
}