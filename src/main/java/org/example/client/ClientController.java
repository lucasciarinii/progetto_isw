package org.example.client;

import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.client.rmi.GameEventListener;
import org.example.client.view.TUI.CLIInputHandler;
import org.example.client.view.TUI.TUIView;
import org.example.network.RankingUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.GamePhase;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.server.model.enums.OfferEffect;
import org.example.server.rmi.RMIGameServer;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;

/*? Client-Side Controller:
    - It connects to the RMI server
    - Send commands to the server (placeTotem, offerTileAction)
    - Receive updates/errors through ClientCallbackImpl and updates the view
 */
public class ClientController implements GameEventListener {
    private final String nickname;
    private RMIGameServer server;       // server stub RMI
    private final TUIView TUIView;
    private final CLIInputHandler inputHandler;

    public ClientController(String nickname) {
        this.nickname = nickname;
        this.TUIView = new TUIView();
        this.inputHandler = new CLIInputHandler(this);
    }

    public String getNickname() { return nickname; }

    public TUIView getView() {
        return TUIView;
    }

    //! CONNECTION TO SERVER -----------------------------------------------
    public void connect(String host, int numPlayers) throws Exception {
        // Forces RMI to use localhost instead of network board IP
        System.setProperty("java.rmi.server.hostname", "localhost");

        // 1. Retrieve the server stub from the registry
        server = (RMIGameServer) Naming.lookup("rmi://" + host + "/GameServer");

        // 2. Create the callback (remote object on client side)
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(this);

        // 3. It registers on the server
        server.register(nickname, numPlayers, callback);
    }

    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        if (update.isGameStarting()) {
            System.out.println("Match is starting!");
        } else {
            System.out.println("In lobby: " + update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players");
            System.out.println("Connected: " + update.getPlayerNicknames());
        }
    }

    //! COMMANDS TO SERVER -----------------------------------------------
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        server.placeTotemOnOfferTile(nickname, tilePosition);
    }

    public void offerTileAction(String cards) throws Exception {
        server.offerTileAction(nickname, cards);
    }

    //! RECEIVING UPDATES FROM SERVER (called by ClientCallbackImpl) -----------------------------------------------
    @Override
    public void onUpdate(GameStateUpdateMessage update) {
        TUIView.update(update);

        if (isMyTurn(update)) {
            if (update.getCurrentPhase() == GamePhase.PLAYER_TURN) {
                OfferEffect effect = update.getOfferTrack().stream()
                        .filter(tile -> nickname.equals(tile.getOccupantNickname()))
                        .map(OfferTileSnapshot::getOfferEffect)
                        .findFirst()
                        .orElse(null);

                if (!hasPickableCards(effect, update)) { // Client-Side check
                    TUIView.displayNoCardsPickable();
                    // Warn server to go on
                    try {
                        server.skipTurn(nickname);
                    } catch (RemoteException e) {
                        TUIView.displayError("Communication error: " + e.getMessage());
                    }
                    return;
                }
            }
            inputHandler.promptForAction(update.getCurrentPhase());
        } else if ( !isMyTurn(update) && isInteractivePhase(update.getCurrentPhase()) ) {
            TUIView.displayWaiting(update.getCurrentPlayerNickname());
        }
    }

    @Override
    public void onError(String errorMessage) {
        TUIView.displayError(errorMessage);
        inputHandler.promptForAction(TUIView.getCurrentPhase()); // in handleOfferTileAction (promptForAction) there is a while loop that will keep asking for input until the server accepts a valid command, so we can just rely on that and do not need to re-prompt here
    }

    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingMessage) {
        TUIView.displayRankingUpdate(rankingMessage.getRanking(), rankingMessage.getPlayerRankPosition());
    }

    @Override
    public void onShutdown() {
        inputHandler.warnExit();
    }

    //! UTILITY METHODS -----------------------------------------------
    private boolean isMyTurn(GameStateUpdateMessage update) {
        return update.getCurrentPlayerNickname().equals(nickname) && isInteractivePhase(update.getCurrentPhase());
    }

    private boolean isInteractivePhase(GamePhase phase) {
        return phase == GamePhase.PLACE_TOTEMS || phase == GamePhase.PLAYER_TURN;
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
