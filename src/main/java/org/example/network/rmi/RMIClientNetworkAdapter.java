package org.example.network.rmi;

import org.example.client.ClientController;
import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.network.ClientNetworkAdapter;

import java.rmi.Naming;

// Client implementation using RMI
public class RMIClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController controller;
    private RMIGameServer server;
    private String nickname;

    public RMIClientNetworkAdapter(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void connect(String host, int port) throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");

        String resolvedHost = host.equalsIgnoreCase("localhost") ? "127.0.0.1" : host;
        String url = "rmi://" + resolvedHost + ":" + port + "/GameServer";

        server = (RMIGameServer) Naming.lookup(url);
    }

    @Override
    public void createLobby(String nickname, int numPlayers) throws Exception {
        this.nickname = nickname;
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(controller);
        server.createGame(nickname, numPlayers, callback);
    }

    @Override
    public void joinLobby(String nickname, String gameID) throws Exception {
        this.nickname = nickname;
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(controller);
        server.joinGame(nickname, gameID, callback);
    }

    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        server.placeTotemOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void offerTileAction(String cards) throws Exception {
        server.offerTileAction(nickname, cards);
    }

    @Override
    public void roundFlowCardRequest(String cards) throws Exception {
        server.roundFlowCardRequest(nickname, cards);
    }

    @Override
    public void skipTurn() throws Exception {
        server.skipTurn(nickname);
    }

    @Override
    public void disconnect() {
        // RMI doesn't request an explicit disconnection
    }
}
