package org.example.client;

import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.client.rmi.GameEventListener;
import org.example.client.view.CLIInputHandler;
import org.example.client.view.View;
import org.example.server.model.enums.GamePhase;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.server.rmi.RMIGameServer;

import java.rmi.Naming;

/*? Client-Side Controller:
    - It connects to the RMI server
    - Send commands to the server (placeTotem, offerTileAction)
    - Receive updates/errors through ClientCallbackImpl and updates the view
 */
public class ClientController implements GameEventListener {
    private final String nickname;
    private RMIGameServer server;       // server stub RMI
    private final View view;
    private final CLIInputHandler inputHandler;

    public ClientController(String nickname) {
        this.nickname = nickname;
        this.view = new View();
        this.inputHandler = new CLIInputHandler(this);
    }

    public View getView() {
        return view;
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
        view.update(update);

        if (isMyTurn(update)) {
            inputHandler.promptForAction(update.getCurrentPhase());
        } else {
            view.displayWaiting(update.getCurrentPlayerNickname());
        }
    }

    @Override
    public void onError(String errorMessage) {
        view.displayError(errorMessage);
        inputHandler.promptForAction(view.getCurrentPhase()); // in handleOfferTileAction (promptForAction) there is a while loop that will keep asking for input until the server accepts a valid command, so we can just rely on that and do not need to re-prompt here
    }

    //! UTILITY METHODS -----------------------------------------------
    private boolean isMyTurn(GameStateUpdateMessage update) {
        return update.getCurrentPlayerNickname().equals(nickname) && isInteractivePhase(update.getCurrentPhase());
    }

    private boolean isInteractivePhase(GamePhase phase) {
        return phase == GamePhase.PLACE_TOTEMS || phase == GamePhase.PLAYER_TURN;
    }
}
