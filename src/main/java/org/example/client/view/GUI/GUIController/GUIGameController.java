package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.client.ClientController;
import org.example.client.view.GUI.components.CardView;
import org.example.client.view.GUI.components.OfferTileView;
import org.example.client.view.GUI.components.PlayerPanelView;
import org.example.client.view.GUI.components.TurnSlotView;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GUIGameController {

    private static final double BOARD_TILE_WIDTH = 120;
    private static final double BOARD_TILE_HEIGHT = 180;

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Label     roundLabel;
    @FXML private Label     eraLabel;
    @FXML private Label     phaseLabel;
    @FXML private Label     currentPlayerLabel;
    @FXML private Label     statusLabel;
    @FXML private Button    confirmButton;
    @FXML private ImageView deckBackView;
    @FXML private HBox      topRowBox;
    @FXML private HBox      bottomRowBox;
    @FXML private HBox      offerTrackBox;
    @FXML private HBox      turnSlotBox;
    @FXML private VBox      opponentsBox;
    @FXML private HBox      localPlayerBox;

    // ── Internal State ─────────────────────────────────────────────────────────
    private ClientController controller;
    private String localNickname;
    private GameStateUpdateMessage lastUpdate;

    // Cards selected by the player
    private final List<CardView> selectedCards = new ArrayList<>();

    // Player panels — Objects kept in memory to update them without recreating them
    private final List<PlayerPanelView> playerPanels = new ArrayList<>();

    // ── Setters called from GUIHandler ────────────────────────────────────────

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public void setLocalNickname(String nickname) {
        this.localNickname = nickname;
    }

    // ── public methods (called from GUIHandler) ───────────────────────────────

    /**
     * Update whole screen with new state of the game.
     * Called for each GameStateUpdateMessage received
     */
    public void update(GameStateUpdateMessage update) {
        this.lastUpdate = update;

        updateInfoBar(update);
        updateDeckBack(update.getCurrentEra());
        updateTopRow(update.getTopRow());
        updateBottomRow(update.getBottomRow());
        updateOfferTrack(update.getOfferTrack());
        updateTurnSlot(update);
        updatePlayerPanels(update.getPlayers());

        // Reset the state at each interaction for each update
        confirmButton.setVisible(false);
        selectedCards.clear();

        // Check game over
        if (!update.getWinners().isEmpty()) {
            showGameOver(update.getWinners());
        }
    }

    /**
     * Enable the interaction for the current phase.
     * Called from GUIHandler after update().
     */
    public void promptForAction(GamePhase phase) {
        switch (phase) {
            case PLACE_TOTEMS -> enablePlaceTotems();
            case PLAYER_TURN  -> enablePlayerTurn();
            default           -> {}
        }
    }

    /**
     * Show an error message in the action bar.
     */
    public void showError(String message) {
        statusLabel.setTextFill(Color.web("#e63946"));
        statusLabel.setText("[ERROR] " + message);

        // Re-enable the interaction using the phase of the last update
        if (lastUpdate != null && lastUpdate.getCurrentPlayerNickname().equals(localNickname)) {
            selectedCards.clear();
            promptForAction(lastUpdate.getCurrentPhase());
        }
    }

    /**
     * Mostra un messaggio informativo quando il giocatore non può selezionare alcuna carta.
     */
    public void showNoCardsPickable() {
        confirmButton.setVisible(false);
        selectedCards.clear();
        statusLabel.setTextFill(Color.web("#a0a080"));
        statusLabel.setText("No selectable card: the turn will be skipped.");
    }

    /**
     * Mostra che è il turno di un altro giocatore.
     */
    public void showWaiting(String currentPlayerNickname) {
        confirmButton.setVisible(false);
        selectedCards.clear();
        statusLabel.setTextFill(Color.web("#a0a080"));
        statusLabel.setText("Turno di " + currentPlayerNickname + "...");
    }

    // ── Aggiornamento barra info ───────────────────────────────────────────────

    private void updateInfoBar(GameStateUpdateMessage update) {
        roundLabel.setText(String.valueOf(update.getCurrentRound()));
        eraLabel.setText(update.getCurrentEra().toString());
        phaseLabel.setText(update.getCurrentPhase().toString());
        currentPlayerLabel.setText(update.getCurrentPlayerNickname());

        // Colora il nome del giocatore corrente con il suo colore totem
        String hex = PlayerColorRegistry.getInstance()
                .getHex(update.getCurrentPlayerNickname());
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + hex + "; " +
                        "-fx-font-size: 12px; -fx-font-weight: bold;"
        );

        // Messaggio stato
        if (update.getCurrentPlayerNickname().equals(localNickname)) {
            statusLabel.setTextFill(Color.web("#00cc66"));
            statusLabel.setText("It's your turn!");
        } else {
            statusLabel.setTextFill(Color.web("#a0a080"));
            statusLabel.setText(update.getCurrentPlayerNickname() + " turn...");
        }
    }

    // ── Aggiornamento retro mazzo ─────────────────────────────────────────────

    private void updateDeckBack(Era era) {
        String filename = switch (era) {
            case I   -> "back_era1.jpg";
            case II  -> "back_era2.jpg";
            case III -> "back_era3.jpg";
        };
        String path = "/images/cards/" + filename;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) deckBackView.setImage(new Image(is));
        } catch (Exception e) {
            System.err.println("[GUIGameController] Retro mazzo non trovato: " + path);
        }
    }

    // ── Aggiornamento righe carte ─────────────────────────────────────────────

    private void updateTopRow(List<Card> cards) {
        topRowBox.getChildren().clear();
        for (Card card : cards) {
            CardView cv = new CardView(card, BOARD_TILE_WIDTH, BOARD_TILE_HEIGHT);
            cv.setState(CardView.State.NORMAL);
            topRowBox.getChildren().add(cv);
        }
    }

    private void updateBottomRow(List<Card> cards) {
        bottomRowBox.getChildren().clear();
        for (Card card : cards) {
            CardView cv = new CardView(card, BOARD_TILE_WIDTH, BOARD_TILE_HEIGHT);
            cv.setState(CardView.State.NORMAL);
            bottomRowBox.getChildren().add(cv);
        }
    }

    // ── Aggiornamento offer track ─────────────────────────────────────────────

    private void updateOfferTrack(List<OfferTileSnapshot> tiles) {
        offerTrackBox.getChildren().clear();
        for (int i = 0; i < tiles.size(); i++) {
            OfferTileView tv = new OfferTileView(tiles.get(i), i + 1);
            offerTrackBox.getChildren().add(tv);
        }
    }

    // ── Aggiornamento turn slot ────────────────────────────────────────────────

    private void updateTurnSlot(GameStateUpdateMessage update) {
        turnSlotBox.getChildren().clear();
        TurnSlotView tsv = new TurnSlotView(update.getTurnOrderSlots());
        turnSlotBox.getChildren().add(tsv);
    }

    // ── Aggiornamento pannelli giocatori ──────────────────────────────────────

    private void updatePlayerPanels(List<PlayerSnapshot> players) {
        if (playerPanels.isEmpty()) {
            // Prima volta: crea i pannelli e li divide tra Destra e Basso
            for (PlayerSnapshot p : players) {
                boolean isLocal = p.getNickname().equals(localNickname);

                // Creiamo il pannello: se NON è il locale, sarà "mini" (true)
                PlayerPanelView panel = new PlayerPanelView(p, isLocal, !isLocal);
                playerPanels.add(panel);

                if (isLocal) {
                    localPlayerBox.getChildren().add(panel); // Tu vai in basso
                } else {
                    opponentsBox.getChildren().add(panel);   // Loro vanno a destra
                }
            }
        } else {
            // Aggiornamenti successivi: aggiorna i dati nei pannelli esistenti
            for (int i = 0; i < playerPanels.size(); i++) {
                playerPanels.get(i).update(players.get(i));
            }
        }
    }

    // ── PLACE_TOTEMS Interaction ───────────────────────────────────────────────

    /**
     * Makes the available tiles on the offer track clickable.
     */
    private void enablePlaceTotems() {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Choose a tile where to place the totem.");

        List<OfferTileView> tileViews = offerTrackBox.getChildren()
                .stream()
                .filter(n -> n instanceof OfferTileView)
                .map(n -> (OfferTileView) n)
                .toList();

        for (OfferTileView tv : tileViews) {
            if (tv.getSnapshot().isFree()) {
                tv.setSelectable(true, () -> {
                    try {
                        controller.placeTotemOnOfferTile(tv.getPosition());
                    } catch (Exception e) {
                        showError(e.getMessage());
                    }
                });
            }
        }
    }

    // ── PLAYER_TURN Interaction ───────────────────────────────────────────────

    /**
     * Enable the selection of the card based on the effect of the tile
     * on witch the local player has placed the totem.
     * It replicates the logic of TUIHandler.handleOfferTileAction
     */
    private void enablePlayerTurn() {
        // Trova l'effetto della tessera occupata dal giocatore locale
        OfferEffect effect = lastUpdate.getOfferTrack().stream()
                .filter(t -> localNickname.equals(t.getOccupantNickname()))
                .map(OfferTileSnapshot::getOfferEffect)
                .findFirst()
                .orElse(null);

        if (effect == null) return;

        // Caso FOOD: nessuna interazione, azione automatica
        if (effect == OfferEffect.FOOD) {
            statusLabel.setTextFill(Color.web("#a0a080"));
            statusLabel.setText("Food Tile — automatic turn.");
            try {
                controller.offerTileAction("");
            } catch (Exception e) {
                showError(e.getMessage());
            }
            return;
        }

        // Find the local player snapshot
        PlayerSnapshot localPlayer = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(localNickname))
                .findFirst()
                .orElseThrow();

        // Calculate how many cards can be drawn from each row
        int fromBottom = 0;
        int fromTop    = 0;

        switch (effect) {
            case D   -> fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
            case DD  -> fromBottom = (int) Math.min(2, countPickable(lastUpdate.getBottomRow(), localPlayer));
            case U   -> fromTop    = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), localPlayer));
            case UU  -> fromTop    = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), localPlayer));
            case DU  -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
                fromTop    = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), localPlayer));
            }
            case DUU -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
                fromTop    = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), localPlayer));
            }
            default -> {}
        }

        int totalToSelect = fromBottom + fromTop;

//        if (totalToSelect == 0) {
//            // Nessuna carta selezionabile
//            statusLabel.setTextFill(Color.web("#a0a080"));
//            statusLabel.setText("Nessuna carta disponibile — turno saltato.");
//            try {
//                controller.offerTileAction("");
//            } catch (Exception e) {
//                showError(e.getMessage());
//            }
//            return;
//        }

        // Enable card selection on the right rows
        enableCardSelection(fromBottom, fromTop, totalToSelect, localPlayer);
    }

    /**
     * Rende selezionabili le carte nelle righe in base ai conteggi calcolati.
     */
    private void enableCardSelection(int fromBottom, int fromTop,
                                     int totalToSelect, PlayerSnapshot localPlayer) {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Select " + totalToSelect + " card/s.");

        // Enable top row cards
        if (fromTop > 0) {
            for (var node : topRowBox.getChildren()) {
                if (node instanceof CardView cv) {
                    if (isPickable(cv.getCard(), localPlayer)) {
                        cv.setSelectable(true, () -> onCardClicked(cv, totalToSelect));
                    } else {
                        cv.setState(CardView.State.DISABLED);
                    }
                }
            }
        } else {
            // Row not usable: disable everything
            topRowBox.getChildren().forEach(n -> {
                if (n instanceof CardView cv) cv.setState(CardView.State.DISABLED);
            });
        }

        // Enable bottom row cards
        if (fromBottom > 0) {
            for (var node : bottomRowBox.getChildren()) {
                if (node instanceof CardView cv) {
                    if (isPickable(cv.getCard(), localPlayer)) {
                        cv.setSelectable(true, () -> onCardClicked(cv, totalToSelect));
                    } else {
                        cv.setState(CardView.State.DISABLED);
                    }
                }
            }
        } else {
            bottomRowBox.getChildren().forEach(n -> {
                if (n instanceof CardView cv) cv.setState(CardView.State.DISABLED);
            });
        }
    }

    /**
     * Handles the click on a selectable card.
     */
    private void onCardClicked(CardView cv, int totalToSelect) {
        if (cv.isSelected()) {
            // Deselect
            cv.toggleSelected();
            selectedCards.remove(cv);
        } else {
            if (selectedCards.size() < totalToSelect) {
                // Select
                cv.toggleSelected();
                selectedCards.add(cv);
            }
        }

        // Update message
        int remaining = totalToSelect - selectedCards.size();
        if (remaining > 0) {
            statusLabel.setTextFill(Color.web("#ffcc00"));
            statusLabel.setText("Select  " + remaining + " other cart/s.");
            confirmButton.setVisible(false);
        } else {
            statusLabel.setTextFill(Color.web("#00cc66"));
            statusLabel.setText("Ready! Confirm the selection.");
            confirmButton.setVisible(true);
        }
    }

    /**
     * Chiamato dal pulsante Conferma in game.fxml.
     */
    @FXML
    private void onConfirm() {
        String payload = selectedCards.stream()
                .map(cv -> String.valueOf(cv.getCard().getId()))
                .collect(Collectors.joining(","));

        confirmButton.setVisible(false);
        selectedCards.clear();

        try {
            controller.offerTileAction(payload);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ── Game over ─────────────────────────────────────────────────────────────

    private void showGameOver(List<String> winners) {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Game ended! Winners: " + String.join(", ", winners));
        confirmButton.setVisible(false);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private long countPickable(List<Card> row, PlayerSnapshot player) {
        return row.stream()
                .filter(c -> isPickable(c, player))
                .count();
    }

    private boolean isPickable(Card card, PlayerSnapshot player) {
        return card.isCharacter() ||
                (card.isBuilding() &&
                        ((BuildingCard) card).getFoodCost() <=
                                player.getFood() + player.getDiscountOnBuilding());
    }
}