package org.example.client;

import org.example.client.rmi.ClientCallbackImpl;
import org.example.client.rmi.ClientCallbackListener;
import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.server.rmi.GameServer;

import java.rmi.Naming;

/*? Client-Side Controller:
    - It connects to the RMI server
    - Send commands to the server (placeTotem, offerTileAction)
    - Receive updates/errors through ClientCallbackImpl and updates the view
 */
public class ClientController implements ClientCallbackListener {
    private final String nickname;
    private GameServer server;       // server stub RMI

    public ClientController(String nickname) {
        this.nickname = nickname;
    }

    //! CONNECTION TO SERVER -----------------------------------------------
    public void connect(String host, int numPlayers) throws Exception {
        // Forces RMI to use localhost instead of network board IP
        System.setProperty("java.rmi.server.hostname", "localhost");

        // 1. Retrieve the server stub from the registry
        server = (GameServer) Naming.lookup("rmi://" + host + "/GameServer");

        // 2. Create the callback (remote object on client side)
        ClientCallbackImpl callback = new ClientCallbackImpl(this);

        // 3. It registers on the server
        server.register(nickname, numPlayers, callback);

        System.out.println("Connected to the server (RMI) as: " + nickname);
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
        // TODO: aggiorna la view con il nuovo stato
        // Per ora stampiamo solo a console
        System.out.println("=== AGGIORNAMENTO STATO ===");
        System.out.println("Round: " + update.getCurrentRound());
        System.out.println("Fase: " + update.getCurrentPhase());
        System.out.println("Turno di: " + update.getCurrentPlayerNickname());
    }

    @Override
    public void onError(String errorMessage) {
        // TODO: mostra errore nella view
        System.out.println("[ERRORE] " + errorMessage);
    }
}
