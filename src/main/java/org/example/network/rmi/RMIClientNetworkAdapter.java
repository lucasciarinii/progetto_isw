package org.example.network.rmi;

import org.example.client.ClientController;
import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.network.ClientNetworkAdapter;

import java.rmi.Naming;

/**
 * RMI-based client adapter that invokes RMIGameServer methods on the server.
 */
public class RMIClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController controller;
    private RMIGameServer server;
    private String nickname;

    /**
     * Creates a client adapter bound to a controller.
     *
     * @param controller the client controller that receives callbacks
     */
    public RMIClientNetworkAdapter(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Connects to the RMI registry and registers the client callback.
     * The server will invoke RMIClientCallback methods to push updates.
     */
    @Override
    public void connect(String host, int port, String nickname, int numPlayers) throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        this.nickname = nickname;

        String resolvedHost = host.equalsIgnoreCase("localhost") ? "127.0.0.1" : host;
        String url = "rmi://" + resolvedHost + ":" + port + "/GameServer";

        server = (RMIGameServer) Naming.lookup(url);

        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(controller);
        server.register(nickname, numPlayers, callback);
    }

    /**
     * Forwards the action to RMIGameServer.placeTotemOnOfferTile.
     */
    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        server.placeTotemOnOfferTile(nickname, tilePosition);
    }

    /**
     * Forwards the action to RMIGameServer.offerTileAction.
     */
    @Override
    public void offerTileAction(String cards) throws Exception {
        server.offerTileAction(nickname, cards);
    }

    /**
     * Forwards the action to RMIGameServer.roundFlowCardRequest.
     */
    @Override
    public void roundFlowCardRequest(String cards) throws Exception {
        server.roundFlowCardRequest(nickname, cards);
    }

    /**
     * Forwards the action to RMIGameServer.skipTurn.
     */
    @Override
    public void skipTurn() throws Exception {
        server.skipTurn(nickname);
    }

    /**
     * RMI does not require an explicit disconnect for this client.
     */
    @Override
    public void disconnect() {
        // RMI doesn't request an explicit disconnection
    }
}
