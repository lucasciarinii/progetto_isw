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

public class PlayerPanelView extends VBox {

    private final PlayerSnapshot snapshot;

    private final Label foodLabel;
    private final Label pointsLabel;
    private final Label discountLabel;
    private final Label totalPointsLabel;

    // Il contenitore dove impileremo i mazzetti di carte
    private final HBox stacksContainer;

    // Variabili per gestire le grandezze in base a "isMini"
    private final double cardW;
    private final double cardH;
    private final double stackOverlap;

    public PlayerPanelView(PlayerSnapshot snapshot, boolean isLocalPlayer, boolean isMini) {
        this.snapshot = snapshot;

        // Impostiamo le grandezze: piccole per gli avversari, grandi per te
        this.cardW = isMini ? 45 : 100;
        this.cardH = isMini ? 65 : 140;
        this.stackOverlap = isMini ? -45 : -80;

        String hex = PlayerColorRegistry.getInstance().getHex(snapshot.getNickname());
        setPadding(new Insets(8));

        // Stile del bordo (più spesso per il giocatore locale)
        String borderWidth = isLocalPlayer ? "3" : "1.5";
        setStyle("-fx-background-color: #1a1a10; -fx-border-color: " + hex +
                "; -fx-border-width: " + borderWidth + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        // --- INTESTAZIONE (Nome + Pallino colorato) ---
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox dot = new VBox();
        dot.setPrefSize(12, 12);
        dot.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 6;");
        Label nicknameLabel = new Label(snapshot.getNickname());
        nicknameLabel.setStyle("-fx-text-fill: " + hex + "; -fx-font-size: " + (isMini ? "11px" : "16px") + "; -fx-font-weight: bold; -fx-background-color: transparent;");
        header.getChildren().addAll(dot, nicknameLabel);

        // --- STATISTICHE (Cibo, Punti, Sconti con sfondi trasparenti) ---
        foodLabel = new Label("🍖 " + snapshot.getFood());
        pointsLabel = new Label("⭐ " + snapshot.getPoints());
        discountLabel = new Label("Sconto Edifici: -" + snapshot.getDiscountOnBuilding());
        totalPointsLabel = new Label("🏆 "  + snapshot.getPoints());

        String statStyle = "-fx-text-fill: #d0c8a0; -fx-font-size: " + (isMini ? "10px" : "13px") + "; -fx-background-color: transparent;";
        foodLabel.setStyle(statStyle);
        pointsLabel.setStyle(statStyle);
        discountLabel.setStyle(statStyle);
        totalPointsLabel.setStyle(statStyle);
        discountLabel.setVisible(snapshot.getDiscountOnBuilding() > 0);

        HBox stats = new HBox(15, foodLabel, pointsLabel, totalPointsLabel);
        stats.setAlignment(Pos.CENTER_LEFT);

        // --- CARTE ---
        stacksContainer = new HBox(12);
        stacksContainer.setAlignment(Pos.TOP_LEFT);
        stacksContainer.setStyle("-fx-background-color: transparent;"); // Evita sfondi di fallback
        renderCards(snapshot);

        ScrollPane scrollPane = new ScrollPane(stacksContainer);
        scrollPane.setFitToHeight(true);

        // Rimuove lo sfondo bianco e il bordo nativo dello ScrollPane direttamente inline
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // --- ASSEMBLAGGIO FLUIDO ---
        if (isMini) {
            // Layout Orizzontale Avversari
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
            // Layout Verticale Locale
            setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);

            // Fissiamo l'altezza per evitare sfasamenti all'aggiunta di carte
            scrollPane.setPrefHeight(180);
            scrollPane.setMinHeight(180);
            scrollPane.setMaxHeight(180);

            Label separator = new Label("─────────────────────────");
            separator.setStyle("-fx-text-fill: #333322; -fx-font-size: 9px; -fx-background-color: transparent;");

            getChildren().addAll(header, stats, discountLabel, separator, scrollPane);
        }
    }

    public void update(PlayerSnapshot newSnapshot) {
        foodLabel.setText("🍖 " + newSnapshot.getFood());
        pointsLabel.setText("⭐ " + newSnapshot.getPoints());
        discountLabel.setText("Sconto Edifici: -" + newSnapshot.getDiscountOnBuilding());
        discountLabel.setVisible(newSnapshot.getDiscountOnBuilding() > 0);
        totalPointsLabel.setText("🏆 " + newSnapshot.getPoints());

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

    // Usa il wildcard "? extends Card" per accettare ogni tipo di carta
    private void addCardStackIfNotEmpty(List<? extends Card> cards) {
        if (cards == null || cards.isEmpty()) return;

        VBox stack = new VBox();
        stack.setSpacing(stackOverlap); // Spaziatura negativa dinamica!
        stack.setAlignment(Pos.TOP_CENTER);
        stack.setPadding(new Insets(0, 0, 10, 0));

        for (Card card : cards) {
            CardView mini = new CardView(card, cardW, cardH);
            mini.setState(CardView.State.NORMAL);
            // Leggera ombra per distinguere le carte sovrapposte
            mini.setStyle(mini.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 4, 0, 0, 2);");
            stack.getChildren().add(mini);
        }

        stacksContainer.getChildren().add(stack);
    }
}