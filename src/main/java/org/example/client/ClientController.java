package org.example.client;

import org.example.client.view.UIHandler;
import org.example.network.ClientNetworkAdapter;
import org.example.network.CommunicationProtocol;
import org.example.network.NetworkAdapterFactory;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.cards.buildingCards.RoundFlowBC;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;

import java.util.List;

/*? Client-Side Controller:
    - It connects to the RMI server
    - Send commands to the server (placeTotem, offerTileAction)
    - Receive updates/errors through ClientCallbackImpl and updates the view
 */
public class ClientController implements GameEventListener {
    private final String nickname;
    private String gameID;
    private ClientNetworkAdapter networkAdapter;
    private final UIHandler ui;
    GameStateUpdateMessage lastGameStateUpdate;

    public ClientController(String nickname, UIHandler ui) {
        this.nickname = nickname;
        this.ui = ui;
    }

    public String getNickname() { return nickname; }

    public void setGameID(String gameID) {
        this.gameID = gameID;
        ui.setGameID(gameID);
    }


    //! CONNECTION TO SERVER -----------------------------------------------
    public void createLobbyAndConnect(String host, int port, int numPlayers, CommunicationProtocol protocol) throws Exception {
        networkAdapter = NetworkAdapterFactory.createClientAdapter(protocol, this);
        networkAdapter.connect(host, port);
        networkAdapter.createLobby(nickname, numPlayers);
    }

    public void joinLobbyAndConnect(String host, int port, String gameID, CommunicationProtocol protocol) throws Exception {
        networkAdapter = NetworkAdapterFactory.createClientAdapter(protocol, this);
        networkAdapter.connect(host, port);
        networkAdapter.joinLobby(nickname, gameID);
    }

    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        ui.onLobbyUpdate(update);
    }

    //! COMMANDS TO SERVER -----------------------------------------------
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        networkAdapter.placeTotemOnOfferTile(tilePosition);
    }

    public void offerTileAction(String cards) throws Exception {
        networkAdapter.offerTileAction(cards);
    }

    public void roundFlowCardRequest(String cards) throws Exception {
        networkAdapter.roundFlowCardRequest(cards);
    }

    //! RECEIVING UPDATES FROM SERVER (called by ClientCallbackImpl) -----------------------------------------------
    @Override
    public void onUpdate(GameStateUpdateMessage update) {
        if (isMyTurn(update)) {
            if (update.getCurrentPhase() == GamePhase.PLAYER_TURN) {
                OfferEffect effect = update.getOfferTrack().stream()
                        .filter(tile -> nickname.equals(tile.getOccupantNickname()))
                        .map(OfferTileSnapshot::getOfferEffect)
                        .findFirst()
                        .orElse(null);
                if (!hasPickableCards(effect, update)) {
                    ui.displayNoCardsPickable();
                    // skipTurn() must be called on a separate thread to avoid a deadlock:
                    // onUpdate() runs on the RMI callback thread, and calling server.skipTurn()
                    // synchronously from it would block that thread while waiting for the server
                    // to respond — which it cannot, since the callback thread is still occupied.
                    new Thread(() -> {
                        try {
                            networkAdapter.skipTurn();
                        } catch (Exception e) {
                            ui.onError(e.getMessage(), update.getCurrentPhase());
                        }
                    }).start();
                    return;
                }
            }
            ui.onGameStateUpdate(update);
            ui.promptForAction(update.getCurrentPhase());
            lastGameStateUpdate = update;
        } else {
            ui.onGameStateUpdate(update);
            lastGameStateUpdate = update;
            if (isRoundFlowPending(update)) {
                if (!update.getCurrentPlayerNickname().equals(nickname)) {
                    ui.displayRoundFlowWaiting(update.getCurrentPlayerNickname());
                }
            } else if (isInteractivePhase(update.getCurrentPhase())) {
                ui.displayWaiting(update.getCurrentPlayerNickname());
            }
        }
    }

    @Override
    public void onError(String errorMessage, GamePhase phase) {
        ui.onError(errorMessage, phase);
    }

    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingMessage) {
        ui.onRankingUpdate(rankingMessage);
    }

    @Override
    public void onRoundFlowCardRequest() {
        if (lastGameStateUpdate == null) {
            ui.onRoundFlowCardRequest();
            return;
        }

        if (!hasPickableCards(OfferEffect.U, lastGameStateUpdate)) {
            ui.displayNoCardsPickable();
            // skipTurn() must be called on a separate thread to avoid a deadlock:
            // onUpdate() runs on the RMI callback thread, and calling server.skipTurn()
            // synchronously from it would block that thread while waiting for the server
            // to respond — which it cannot, since the callback thread is still occupied.
            new Thread(() -> {
                try {
                    networkAdapter.skipTurn();
                } catch (Exception e) {
                    ui.onError(e.getMessage(), lastGameStateUpdate.getCurrentPhase());
                }
            }).start();
            return;
        }

        ui.onRoundFlowCardRequest();
    }


    @Override
    public void onShutdown() {
        ui.onShutdown();
    }

    //! UTILITY METHODS -----------------------------------------------
    private boolean isMyTurn(GameStateUpdateMessage update) {
        return update.getCurrentPlayerNickname().equals(nickname) && isInteractivePhase(update.getCurrentPhase());
    }

    private boolean isInteractivePhase(GamePhase phase) {
        return phase == GamePhase.PLACE_TOTEMS || phase == GamePhase.PLAYER_TURN;
    }

    private boolean isRoundFlowPending(GameStateUpdateMessage update) {
        if (update.getCurrentPhase() != GamePhase.END_ROUND) {
            return false;
        }
        return update.getPlayers().stream()
                .filter(p -> p.getNickname().equals(update.getCurrentPlayerNickname()))
                .findFirst()
                .map(p -> p.getOwnedBuildings().stream().anyMatch(b -> b instanceof RoundFlowBC))
                .orElse(false);
    }

    private long countPickable(List<Card> row, PlayerSnapshot player) {
        return row.stream()
                .filter(c -> c.isCharacter() ||
                        (c.isBuilding() &&
                                ((BuildingCard) c).getFoodCost() <= player.getFood()
                                        + player.getDiscountOnBuilding()))
                .count();
    }

    private boolean hasPickableCards(OfferEffect effect, GameStateUpdateMessage update) {
        if (effect == null) return false;

        PlayerSnapshot player = update.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + nickname));

        return switch (effect) {
            case D, DD  -> countPickable(update.getBottomRow(), player) > 0;
            case U, UU  -> countPickable(update.getTopRow(), player) > 0;
            case DU, DUU -> countPickable(update.getBottomRow(), player) > 0
                    || countPickable(update.getTopRow(), player) > 0;
            case FOOD   -> true;
        };
    }
}
