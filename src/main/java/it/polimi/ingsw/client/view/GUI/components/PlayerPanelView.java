package it.polimi.ingsw.client.view.GUI.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import it.polimi.ingsw.client.view.GUI.registry.PlayerColorRegistry;
import it.polimi.ingsw.network.snapshots.PlayerSnapshot;
import it.polimi.ingsw.server.model.cards.Card;

import java.util.List;

/**
 * Player panel component showing stats and owned cards.
 */
public class PlayerPanelView extends VBox {

    private final Label foodLabel;
    private final Label discountLabel;
    private final Label totalPointsLabel;

    /** Container that holds the rendered card stacks. */
    private final HBox stacksContainer;

    /** Width of each rendered card. */
    private final double cardW;

    /** Height of each rendered card. */
    private final double cardH;

    /** Vertical overlap between cards in the same stack. */
    private final double stackOverlap;

    /**
     * Constructs a player panel showing the player's public information and owned cards.
     * The layout and card sizes change depending on whether the panel represents
     * the local player or a smaller opponent view.
     *
     * @param snapshot the snapshot containing the player data to display
     * @param isLocalPlayer {@code true} if this panel represents the local player,
     *                      {@code false} otherwise
     */
    public PlayerPanelView(PlayerSnapshot snapshot, boolean isLocalPlayer) {
        boolean isMini = !isLocalPlayer; // Opponent panels are mini by default

        // Use smaller sizes for opponents and larger for the local player
        this.cardW = isMini ? 45 : 100;
        this.cardH = isMini ? 65 : 140;
        this.stackOverlap = isMini ? -45 : -80;

        String hex = PlayerColorRegistry.getInstance().getHex(snapshot.getNickname());
        setPadding(new Insets(8));

        // Border style (thicker for the local player)
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
        setMaxWidth(Double.MAX_VALUE);
        if (isMini) {
            // Horizontal layout for opponents
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

    /**
     * Updates the panel contents with a new player snapshot, refreshing
     * both the statistics and the rendered card stacks.
     *
     * @param newSnapshot the updated snapshot to display
     */
    public void update(PlayerSnapshot newSnapshot) {
        foodLabel.setText("Food: " + newSnapshot.getFood());
        discountLabel.setText("Discount on Buildings: -" + newSnapshot.getDiscountOnBuilding());
        discountLabel.setVisible(newSnapshot.getDiscountOnBuilding() > 0);
        totalPointsLabel.setText("Points: " + newSnapshot.getPoints());

        stacksContainer.getChildren().clear();
        renderCards(newSnapshot);
    }

    /**
     * Renders all owned card groups contained in the given snapshot.
     *
     * @param s the snapshot whose owned cards must be displayed
     */
    private void renderCards(PlayerSnapshot s) {
        addCardStackIfNotEmpty(s.getOwnedHunters());
        addCardStackIfNotEmpty(s.getOwnedGatherers());
        addCardStackIfNotEmpty(s.getOwnedBuilders());
        addCardStackIfNotEmpty(s.getOwnedShamans());
        addCardStackIfNotEmpty(s.getOwnedArtists());
        addCardStackIfNotEmpty(s.getOwnedInventors());
        addCardStackIfNotEmpty(s.getOwnedBuildings());
    }


    /**
     * Renders a list of cards as an overlapping vertical stack and adds it to the player's panel.
     * If the provided list is {@code null} or empty, the method safely returns without modifying the UI.
     *
     * @param cards the collection of cards to be displayed belonging to the same category
     */
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