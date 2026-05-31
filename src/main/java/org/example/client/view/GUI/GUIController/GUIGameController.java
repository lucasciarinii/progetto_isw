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
import org.example.server.model.cards.buildingCards.RoundFlowBC;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the main game screen.
 * It renders the full match state and manages user interactions during the match.
 */
public class GUIGameController {

    private static final double BOARD_TILE_WIDTH = 120;
    private static final double BOARD_TILE_HEIGHT = 180;

    // FXML bindings
    @FXML private ImageView bgImageView;
    @FXML private Label roundLabel;
    @FXML private Label eraLabel;
    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label statusLabel;
    @FXML private Button confirmButton;
    @FXML private ImageView deckBackView;
    @FXML private HBox topRowBox;
    @FXML private HBox bottomRowBox;
    @FXML private HBox offerTrackBox;
    @FXML private HBox turnSlotBox;
    @FXML private VBox opponentsBox;
    @FXML private HBox localPlayerBox;

    // Internal state
    private ClientController controller;
    private String localNickname;
    private GameStateUpdateMessage lastUpdate;

    // Cards selected by the player
    private final List<CardView> selectedCards = new ArrayList<>();
    private boolean roundFlowMode = false;

    // Player panels cached to update them without recreating them every time
    private final List<PlayerPanelView> playerPanels = new ArrayList<>();

    /**
     * Initializes the controller after FXML loading.
     * The background image is bound to the actual scene size so that it scales
     * correctly when the window becomes fullscreen.
     */
    @FXML
    private void initialize() {
        bgImageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                bgImageView.fitWidthProperty().bind(newScene.widthProperty());
                bgImageView.fitHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    /**
     * Injects the client controller used to send actions to the server.
     *
     * @param controller the client controller
     */
    public void setController(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Sets the nickname of the local player.
     *
     * @param nickname the local player's nickname
     */
    public void setLocalNickname(String nickname) {
        this.localNickname = nickname;
    }

    /**
     * Updates the entire screen with a fresh game state snapshot.
     *
     * @param update the latest game state update
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

        hideConfirmButton();
        selectedCards.clear();
        roundFlowMode = false;

        if (!update.getWinners().isEmpty()) {
            showGameOver(update.getWinners());
        }

        if (isRoundFlowPhase() && update.getCurrentPlayerNickname().equals(localNickname)) {
            promptRoundFlowPick();
        }
    }

    /**
     * Enables interaction for the current game phase.
     *
     * @param phase the current phase
     */
    public void promptForAction(GamePhase phase) {
        switch (phase) {
            case PLACE_TOTEMS -> enablePlaceTotems();
            case PLAYER_TURN -> enablePlayerTurn();
            default -> { }
        }
    }

    /**
     * Prompts the local player to choose an extra top-row card for RoundFlow.
     */
    public void promptRoundFlowPick() {
        if (lastUpdate == null) {
            return;
        }

        PlayerSnapshot localPlayer = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(localNickname))
                .findFirst()
                .orElseThrow();

        roundFlowMode = true;
        hideConfirmButton();
        selectedCards.clear();
        enableRoundFlowSelection(localPlayer);
    }

    /**
     * Displays an error message in the top status area and re-enables interaction if needed.
     *
     * @param message the error message
     */
    public void showError(String message) {
        statusLabel.setTextFill(Color.web("#e63946"));
        statusLabel.setText("[ERROR] " + message);

        if (lastUpdate != null && lastUpdate.getCurrentPlayerNickname().equals(localNickname)) {
            selectedCards.clear();
            if (isRoundFlowPhase()) {
                promptRoundFlowPick();
            } else {
                promptForAction(lastUpdate.getCurrentPhase());
            }
        }
    }

    public void showGameAborted() {
        statusLabel.setTextFill(Color.web("#e63946"));
        statusLabel.setText("CONNECTION LOST - GAME ABORTED - Please close the game.");
        selectedCards.clear();
    }

    /**
     * Shows an informational message when no selectable cards are available.
     */
    public void showNoCardsPickable() {
        hideConfirmButton();
        selectedCards.clear();
        statusLabel.setTextFill(Color.web("#d8c78f"));
        statusLabel.setText("No selectable card: the turn will be skipped.");
    }

    /**
     * Shows a waiting message while another player is taking their turn.
     *
     * @param currentPlayerNickname the nickname of the player currently playing
     */
    public void showWaiting(String currentPlayerNickname) {
        hideConfirmButton();
        selectedCards.clear();
        statusLabel.setTextFill(Color.web("#d8c78f"));
        statusLabel.setText(currentPlayerNickname + "'s turn...");
    }

    /**
     * Shows a waiting message while another player is resolving RoundFlow.
     *
     * @param currentPlayerNickname the nickname of the player currently choosing
     */
    public void showRoundFlowWaiting(String currentPlayerNickname) {
        hideConfirmButton();
        selectedCards.clear();
        statusLabel.setTextFill(Color.web("#d8c78f"));
        statusLabel.setText(currentPlayerNickname + " is picking an extra card (RoundFlow)...");
    }

    /**
     * Updates the top floating information panel.
     *
     * @param update the latest game state update
     */
    private void updateInfoBar(GameStateUpdateMessage update) {
        roundLabel.setText(String.valueOf(update.getCurrentRound()));
        eraLabel.setText(update.getCurrentEra().toString());
        phaseLabel.setText(update.getCurrentPhase().toString());
        currentPlayerLabel.setText(update.getCurrentPlayerNickname());

        String hex = PlayerColorRegistry.getInstance().getHex(update.getCurrentPlayerNickname());
        currentPlayerLabel.setStyle(
                "-fx-text-fill: " + hex + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        if (update.getCurrentPlayerNickname().equals(localNickname)) {
            statusLabel.setTextFill(Color.web("#00cc66"));
            statusLabel.setText("It's your turn!");
        } else {
            statusLabel.setTextFill(Color.web("#d8c78f"));
            statusLabel.setText(update.getCurrentPlayerNickname() + "'s turn...");
        }
    }

    /**
     * Updates the deck back image according to the current era.
     *
     * @param era the current era
     */
    private void updateDeckBack(Era era) {
        String filename = switch (era) {
            case I -> "back_era1.jpg";
            case II -> "back_era2.jpg";
            case III -> "back_era3.jpg";
        };

        String path = "/images/cards/" + filename;

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                deckBackView.setImage(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("[GUIGameController] Deck back not found: " + path);
        }
    }

    /**
     * Renders the top row of cards.
     *
     * @param cards the cards to display
     */
    private void updateTopRow(List<Card> cards) {
        topRowBox.getChildren().clear();

        for (Card card : cards) {
            CardView cardView = new CardView(card, BOARD_TILE_WIDTH, BOARD_TILE_HEIGHT);
            cardView.setState(CardView.State.NORMAL);
            topRowBox.getChildren().add(cardView);
        }
    }

    /**
     * Renders the bottom row of cards.
     *
     * @param cards the cards to display
     */
    private void updateBottomRow(List<Card> cards) {
        bottomRowBox.getChildren().clear();

        for (Card card : cards) {
            CardView cardView = new CardView(card, BOARD_TILE_WIDTH, BOARD_TILE_HEIGHT);
            cardView.setState(CardView.State.NORMAL);
            bottomRowBox.getChildren().add(cardView);
        }
    }

    /**
     * Renders the offer track.
     *
     * @param tiles the offer-track tiles to display
     */
    private void updateOfferTrack(List<OfferTileSnapshot> tiles) {
        offerTrackBox.getChildren().clear();

        for (int i = 0; i < tiles.size(); i++) {
            OfferTileView tileView = new OfferTileView(tiles.get(i), i + 1);
            offerTrackBox.getChildren().add(tileView);
        }
    }

    /**
     * Renders the turn-order slots.
     *
     * @param update the latest game state update
     */
    private void updateTurnSlot(GameStateUpdateMessage update) {
        turnSlotBox.getChildren().clear();
        TurnSlotView turnSlotView = new TurnSlotView(update.getTurnOrderSlots());
        turnSlotBox.getChildren().add(turnSlotView);
    }

    /**
     * Updates the player panels for both opponents and local player.
     *
     * @param players the players to render
     */
    private void updatePlayerPanels(List<PlayerSnapshot> players) {
        if (playerPanels.isEmpty()) {
            for (PlayerSnapshot player : players) {
                boolean isLocal = player.getNickname().equals(localNickname);
                PlayerPanelView panel = new PlayerPanelView(player, isLocal, !isLocal);
                playerPanels.add(panel);

                if (isLocal) {
                    localPlayerBox.getChildren().add(panel);
                } else {
                    opponentsBox.getChildren().add(panel);
                }
            }
        } else {
            for (int i = 0; i < playerPanels.size(); i++) {
                playerPanels.get(i).update(players.get(i));
            }
        }
    }

    /**
     * Enables selection of free offer-track tiles during the PLACE_TOTEMS phase.
     */
    private void enablePlaceTotems() {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Choose a tile where to place the totem.");

        List<OfferTileView> tileViews = offerTrackBox.getChildren().stream()
                .filter(node -> node instanceof OfferTileView)
                .map(node -> (OfferTileView) node)
                .toList();

        for (OfferTileView tileView : tileViews) {
            if (tileView.getSnapshot().isFree()) {
                tileView.setSelectable(true, () -> {
                    try {
                        controller.placeTotemOnOfferTile(tileView.getPosition());
                    } catch (Exception e) {
                        showError(e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Enables interaction during the PLAYER_TURN phase according to the offer tile effect.
     */
    private void enablePlayerTurn() {
        OfferEffect effect = lastUpdate.getOfferTrack().stream()
                .filter(tile -> localNickname.equals(tile.getOccupantNickname()))
                .map(OfferTileSnapshot::getOfferEffect)
                .findFirst()
                .orElse(null);

        if (effect == null) {
            return;
        }

        if (effect == OfferEffect.FOOD) {
            statusLabel.setTextFill(Color.web("#d8c78f"));
            statusLabel.setText("Food tile — automatic turn.");
            try {
                controller.offerTileAction("");
            } catch (Exception e) {
                showError(e.getMessage());
            }
            return;
        }

        PlayerSnapshot localPlayer = lastUpdate.getPlayers().stream()
                .filter(player -> player.getNickname().equals(localNickname))
                .findFirst()
                .orElseThrow();

        int fromBottom = 0;
        int fromTop = 0;

        switch (effect) {
            case D -> fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
            case DD -> fromBottom = (int) Math.min(2, countPickable(lastUpdate.getBottomRow(), localPlayer));
            case U -> fromTop = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), localPlayer));
            case UU -> fromTop = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), localPlayer));
            case DU -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
                fromTop = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), localPlayer));
            }
            case DUU -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), localPlayer));
                fromTop = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), localPlayer));
            }
            default -> { }
        }

        int totalToSelect = fromBottom + fromTop;
        enableCardSelection(fromBottom, fromTop, totalToSelect, localPlayer);
    }

    /**
     * Enables selection on the top and/or bottom row according to the required counts.
     *
     * @param fromBottom number of cards to pick from the bottom row
     * @param fromTop number of cards to pick from the top row
     * @param totalToSelect total number of cards to select
     * @param localPlayer local player snapshot
     */
    private void enableCardSelection(int fromBottom, int fromTop, int totalToSelect, PlayerSnapshot localPlayer) {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Select " + totalToSelect + " card(s).");

        if (fromTop > 0) {
            for (var node : topRowBox.getChildren()) {
                if (node instanceof CardView cardView) {
                    if (isPickable(cardView.getCard(), localPlayer)) {
                        cardView.setSelectable(true, () -> onCardClicked(cardView, totalToSelect));
                    } else {
                        cardView.setState(CardView.State.DISABLED);
                    }
                }
            }
        } else {
            topRowBox.getChildren().forEach(node -> {
                if (node instanceof CardView cardView) {
                    cardView.setState(CardView.State.DISABLED);
                }
            });
        }

        if (fromBottom > 0) {
            for (var node : bottomRowBox.getChildren()) {
                if (node instanceof CardView cardView) {
                    if (isPickable(cardView.getCard(), localPlayer)) {
                        cardView.setSelectable(true, () -> onCardClicked(cardView, totalToSelect));
                    } else {
                        cardView.setState(CardView.State.DISABLED);
                    }
                }
            }
        } else {
            bottomRowBox.getChildren().forEach(node -> {
                if (node instanceof CardView cardView) {
                    cardView.setState(CardView.State.DISABLED);
                }
            });
        }
    }

    /**
     * Enables top-row-only selection for the RoundFlow extra-card effect.
     *
     * @param localPlayer local player snapshot
     */
    private void enableRoundFlowSelection(PlayerSnapshot localPlayer) {
        int totalToSelect = 1;
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Pick 1 extra card from the top row (RoundFlow).");

        for (var node : topRowBox.getChildren()) {
            if (node instanceof CardView cardView) {
                if (isPickable(cardView.getCard(), localPlayer)) {
                    cardView.setSelectable(true, () -> onCardClicked(cardView, totalToSelect));
                } else {
                    cardView.setState(CardView.State.DISABLED);
                }
            }
        }

        bottomRowBox.getChildren().forEach(node -> {
            if (node instanceof CardView cardView) {
                cardView.setState(CardView.State.DISABLED);
            }
        });
    }

    /**
     * Handles the click on a selectable card.
     *
     * @param cardView the clicked card view
     * @param totalToSelect the total number of cards required
     */
    private void onCardClicked(CardView cardView, int totalToSelect) {
        if (cardView.isSelected()) {
            cardView.toggleSelected();
            selectedCards.remove(cardView);
        } else if (selectedCards.size() < totalToSelect) {
            cardView.toggleSelected();
            selectedCards.add(cardView);
        }

        int remaining = totalToSelect - selectedCards.size();

        if (remaining > 0) {
            statusLabel.setTextFill(Color.web("#ffcc00"));
            if (roundFlowMode) {
                statusLabel.setText("Select the extra card from the top row.");
            } else {
                statusLabel.setText("Select " + remaining + " other card(s).");
            }
            hideConfirmButton();
        } else {
            statusLabel.setTextFill(Color.web("#00cc66"));
            if (roundFlowMode) {
                statusLabel.setText("Ready! Confirm the extra card.");
            } else {
                statusLabel.setText("Ready! Confirm the selection.");
            }
            showConfirmButton();
        }
    }

    /**
     * Sends the selected cards to the server when the confirm button is pressed.
     */
    @FXML
    private void onConfirm() {
        String payload = selectedCards.stream()
                .map(cardView -> String.valueOf(cardView.getCard().getId()))
                .collect(Collectors.joining(","));

        hideConfirmButton();
        selectedCards.clear();

        try {
            if (roundFlowMode) {
                controller.roundFlowCardRequest(payload);
                roundFlowMode = false;
            } else {
                controller.offerTileAction(payload);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Displays the game-over message.
     *
     * @param winners the list of winners
     */
    private void showGameOver(List<String> winners) {
        statusLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setText("Game ended! Winners: " + String.join(", ", winners));
        hideConfirmButton();
    }

    /**
     * Counts how many cards in a row are currently pickable by the player.
     *
     * @param row the row to inspect
     * @param player the player trying to pick
     * @return number of pickable cards
     */
    private long countPickable(List<Card> row, PlayerSnapshot player) {
        return row.stream()
                .filter(card -> isPickable(card, player))
                .count();
    }

    /**
     * Checks whether a card can be picked by the given player.
     *
     * @param card the card to inspect
     * @param player the player attempting to pick
     * @return true if the card is pickable, false otherwise
     */
    private boolean isPickable(Card card, PlayerSnapshot player) {
        return card.isCharacter()
                || (card.isBuilding()
                && ((BuildingCard) card).getFoodCost() <= player.getFood() + player.getDiscountOnBuilding());
    }

    /**
     * Checks whether the local player is in the RoundFlow extra-card step.
     *
     * @return true if the local player must resolve a RoundFlow extra pick
     */
    private boolean isRoundFlowPhase() {
        if (lastUpdate == null || lastUpdate.getCurrentPhase() != GamePhase.END_ROUND) {
            return false;
        }

        return lastUpdate.getPlayers().stream()
                .filter(player -> player.getNickname().equals(localNickname))
                .findFirst()
                .map(player -> player.getOwnedBuildings().stream().anyMatch(building -> building instanceof RoundFlowBC))
                .orElse(false);
    }

    /**
     * Makes the confirm button visible and managed in the layout.
     */
    private void showConfirmButton() {
        confirmButton.setVisible(true);
        confirmButton.setManaged(true);
    }

    /**
     * Hides the confirm button and removes it from layout calculations.
     */
    private void hideConfirmButton() {
        confirmButton.setVisible(false);
        confirmButton.setManaged(false);
    }
}