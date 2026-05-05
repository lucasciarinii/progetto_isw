package org.example.client.view.GUI.GUIController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.example.client.ClientController;
import org.example.client.view.GUI.components.*;
import org.example.client.view.GUI.registry.PlayerColorRegistry;
import org.example.network.GameStateUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
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
    @FXML private HBox      playersBox;

    // ── Stato interno ─────────────────────────────────────────────────────────
    private ClientController controller;
    private String localNickname;
    private GameStateUpdateMessage lastUpdate;

    // Carte attualmente selezionate dal giocatore
    private final List<CardView> selectedCards = new ArrayList<>();

    // Pannelli giocatori — tenuti in memoria per aggiornarli senza ricrearli
    private final List<PlayerPanelView> playerPanels = new ArrayList<>();

    // ── Setters chiamati da GUIHandler ────────────────────────────────────────

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public void setLocalNickname(String nickname) {
        this.localNickname = nickname;
    }

    // ── Metodi pubblici (chiamati da GUIHandler) ───────────────────────────────

    /**
     * Aggiorna l'intera schermata con il nuovo stato della partita.
     * Chiamato ad ogni GameStateUpdateMessage.
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

        // Resetta stato interazione ad ogni update
        confirmButton.setVisible(false);
        selectedCards.clear();

        // Controlla game over
        if (!update.getWinners().isEmpty()) {
            showGameOver(update.getWinners());
        }
    }

    /**
     * Abilita l'interazione per la fase corrente.
     * Chiamato da GUIHandler dopo update().
     */
    public void promptForAction(GamePhase phase) {
        switch (phase) {
            case PLACE_TOTEMS -> enablePlaceTotems();
            case PLAYER_TURN  -> enablePlayerTurn();
            default           -> {}
        }
    }

    /**
     * Mostra un messaggio di errore nella barra azioni.
     */
    public void showError(String message) {
        statusLabel.setTextFill(Color.web("#e63946"));
        statusLabel.setText("⚠ " + message);

        // Riabilita l'interazione usando la fase dell'ultimo update
        if (lastUpdate != null && lastUpdate.getCurrentPlayerNickname().equals(localNickname)) {
            promptForAction(lastUpdate.getCurrentPhase());
        }
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
            statusLabel.setText("È il tuo turno!");
        } else {
            statusLabel.setTextFill(Color.web("#a0a080"));
            statusLabel.setText("Turno di " + update.getCurrentPlayerNickname() + "...");
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
            CardView cv = new CardView(card, 80, 115);
            cv.setState(CardView.State.NORMAL);
            topRowBox.getChildren().add(cv);
        }
    }

    private void updateBottomRow(List<Card> cards) {
        bottomRowBox.getChildren().clear();
        for (Card card : cards) {
            CardView cv = new CardView(card, 80, 115);
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
            // Prima volta: crea i pannelli
            for (PlayerSnapshot p : players) {
                boolean isLocal = p.getNickname().equals(localNickname);
                PlayerPanelView panel = new PlayerPanelView(p, isLocal);
                playerPanels.add(panel);
                playersBox.getChildren().add(panel);
            }
        } else {
            // Aggiornamenti successivi: aggiorna i pannelli esistenti
            for (int i = 0; i < playerPanels.size(); i++) {
                playerPanels.get(i).update(players.get(i));
            }
        }
    }

    // ── Interazione PLACE_TOTEMS ───────────────────────────────────────────────

    /**
     * Rende cliccabili le tessere libere dell'offer track.
     */
    private void enablePlaceTotems() {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Scegli una tessera dove piazzare il tuo totem.");

        List<OfferTileView> tileViews = offerTrackBox.getChildren()
                .stream()
                .filter(n -> n instanceof OfferTileView)
                .map(n -> (OfferTileView) n)
                .collect(Collectors.toList());

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

    // ── Interazione PLAYER_TURN ───────────────────────────────────────────────

    /**
     * Abilita la selezione delle carte in base all'effetto della tessera
     * su cui il giocatore locale ha piazzato il totem.
     * Replica la logica di TUIHandler.handleOfferTileAction().
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
            statusLabel.setText("Tessera cibo — turno automatico.");
            try {
                controller.offerTileAction("");
            } catch (Exception e) {
                showError(e.getMessage());
            }
            return;
        }

        // Trova lo snapshot del giocatore locale
        PlayerSnapshot localPlayer = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(localNickname))
                .findFirst()
                .orElseThrow();

        // Calcola quante carte può prendere da ogni riga
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

        // Abilita selezione carte nelle righe corrette
        enableCardSelection(fromBottom, fromTop, totalToSelect, localPlayer);
    }

    /**
     * Rende selezionabili le carte nelle righe in base ai conteggi calcolati.
     */
    private void enableCardSelection(int fromBottom, int fromTop,
                                     int totalToSelect, PlayerSnapshot localPlayer) {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Seleziona " + totalToSelect + " carta/e.");

        // Abilita carte top row
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
            // Riga non usabile: disabilita tutto
            topRowBox.getChildren().forEach(n -> {
                if (n instanceof CardView cv) cv.setState(CardView.State.DISABLED);
            });
        }

        // Abilita carte bottom row
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
     * Gestisce il click su una carta selezionabile.
     */
    private void onCardClicked(CardView cv, int totalToSelect) {
        if (cv.isSelected()) {
            // Deseleziona
            cv.toggleSelected();
            selectedCards.remove(cv);
        } else {
            if (selectedCards.size() < totalToSelect) {
                // Seleziona
                cv.toggleSelected();
                selectedCards.add(cv);
            }
        }

        // Aggiorna messaggio
        int remaining = totalToSelect - selectedCards.size();
        if (remaining > 0) {
            statusLabel.setTextFill(Color.web("#ffcc00"));
            statusLabel.setText("Seleziona ancora " + remaining + " carta/e.");
            confirmButton.setVisible(false);
        } else {
            statusLabel.setTextFill(Color.web("#00cc66"));
            statusLabel.setText("Pronto! Conferma la selezione.");
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
        statusLabel.setText("🏆 Partita terminata! Vincitori: " + String.join(", ", winners));
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