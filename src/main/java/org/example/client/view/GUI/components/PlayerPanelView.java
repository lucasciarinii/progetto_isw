package org.example.client.view.GUI.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;

import java.util.List;

/**
 * Player panel component showing stats and owned cards.
 */
public class PlayerPanelView extends VBox {

    private final PlayerSnapshot snapshot;

    private final Label foodLabel;
    private final Label discountLabel;
    private final Label totalPointsLabel;

    // Container where card stacks are placed.
    private final HBox stacksContainer;

    // Size settings based on the "isMini" flag.
    private final double cardW;
    private final double cardH;
    private final double stackOverlap;

    public PlayerPanelView(PlayerSnapshot snapshot, boolean isLocalPlayer, boolean isMini) {
        this.snapshot = snapshot;

        // Use smaller sizes for opponents and larger for the local player.
        this.cardW = isMini ? 45 : 100;
        this.cardH = isMini ? 65 : 140;
        this.stackOverlap = isMini ? -45 : -80;

        String hex = PlayerColorRegistry.getInstance().getHex(snapshot.getNickname());
        setPadding(new Insets(8));

        // Border style (thicker for the local player).
        String borderWidth = isLocalPlayer ? "3" : "1.5";
        setStyle("-fx-background-color: #1a1a10; -fx-border-color: " + hex +
                "; -fx-border-width: " + borderWidth + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Header (name + colored dot)
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox dot = new VBox();
        dot.setPrefSize(12, 12);
        dot.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 6;");
        Label nicknameLabel = new Label(snapshot.getNickname());
        nicknameLabel.setStyle("-fx-text-fill: " + hex + "; -fx-font-size: " + (isMini ? "11px" : "16px") + "; -fx-font-weight: bold; -fx-background-color: transparent;");
        header.getChildren().addAll(dot, nicknameLabel);

        // Stats (food, points, discounts)
        foodLabel = new Label("Food: " + snapshot.getFood());
        discountLabel = new Label("Discount on Buildings: -" + snapshot.getDiscountOnBuilding());
        totalPointsLabel = new Label("Points: "  + snapshot.getPoints());

        String statStyle = "-fx-text-fill: #d0c8a0; -fx-font-size: " + (isMini ? "10px" : "13px") + "; -fx-background-color: transparent;";
        foodLabel.setStyle(statStyle);
        discountLabel.setStyle(statStyle);
        totalPointsLabel.setStyle(statStyle);
        discountLabel.setVisible(snapshot.getDiscountOnBuilding() > 0);

        HBox stats = new HBox(15, foodLabel, totalPointsLabel);
        stats.setAlignment(Pos.CENTER_LEFT);

        // Cards
        stacksContainer = new HBox(12);
        stacksContainer.setAlignment(Pos.TOP_LEFT);
        stacksContainer.setStyle("-fx-background-color: transparent;"); // Evita sfondi di fallback
        renderCards(snapshot);

        ScrollPane scrollPane = new ScrollPane(stacksContainer);
        scrollPane.setFitToHeight(true);

        // Remove native ScrollPane background and border.
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Layout assembly
        if (isMini) {
            // Horizontal layout for opponents
            setMaxWidth(Double.MAX_VALUE);
            scrollPane.setPrefHeight(90);

            VBox infoBox = new VBox(5, header, stats, discountLabel);
            infoBox.setMinWidth(140);
            infoBox.setPrefWidth(140);
            infoBox.setStyle("-fx-background-color: transparent;");

            HBox miniLayout = new HBox(10, infoBox, scrollPane);
            miniLayout.setAlignment(Pos.CENTER_LEFT);
            miniLayout.setStyle("-fx-background-color: transparent;");

            HBox.setHgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

            getChildren().add(miniLayout);
        } else {
            // Vertical layout for local player
            setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);

            // Fix height to avoid layout shifts when cards are added.
            scrollPane.setPrefHeight(180);
            scrollPane.setMinHeight(180);
            scrollPane.setMaxHeight(180);

            Label separator = new Label("─────────────────────────");
            separator.setStyle("-fx-text-fill: #333322; -fx-font-size: 9px; -fx-background-color: transparent;");

            getChildren().addAll(header, stats, discountLabel, separator, scrollPane);
        }
    }

    public void update(PlayerSnapshot newSnapshot) {
        foodLabel.setText("Food: " + newSnapshot.getFood());
        discountLabel.setText("Discount on Buildings: -" + newSnapshot.getDiscountOnBuilding());
        discountLabel.setVisible(newSnapshot.getDiscountOnBuilding() > 0);
        totalPointsLabel.setText("Points: " + newSnapshot.getPoints());

        stacksContainer.getChildren().clear();
        renderCards(newSnapshot);
    }

    private void renderCards(PlayerSnapshot s) {
        addCardStackIfNotEmpty(s.getOwnedHunters());
        addCardStackIfNotEmpty(s.getOwnedGatherers());
        addCardStackIfNotEmpty(s.getOwnedBuilders());
        addCardStackIfNotEmpty(s.getOwnedShamans());
        addCardStackIfNotEmpty(s.getOwnedArtists());
        addCardStackIfNotEmpty(s.getOwnedInventors());
        addCardStackIfNotEmpty(s.getOwnedBuildings());
    }

    // Use wildcard to accept any card type.
    private void addCardStackIfNotEmpty(List<? extends Card> cards) {
        if (cards == null || cards.isEmpty()) return;

        VBox stack = new VBox();
        stack.setSpacing(stackOverlap); // Dynamic negative spacing.
        stack.setAlignment(Pos.TOP_CENTER);
        stack.setPadding(new Insets(0, 0, 10, 0));

        for (Card card : cards) {
            CardView mini = new CardView(card, cardW, cardH);
            mini.setState(CardView.State.NORMAL);
            // Subtle shadow to separate overlapping cards.
            mini.setStyle(mini.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 4, 0, 0, 2);");
            stack.getChildren().add(mini);
        }

        stacksContainer.getChildren().add(stack);
    }
}