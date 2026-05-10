package org.example.network.socket;

import org.example.client.ClientController;
import org.example.network.ClientNetworkAdapter;

public class SocketClientNetworkAdapter implements ClientNetworkAdapter {

    private final ClientController controller;

    public SocketClientNetworkAdapter(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void connect(String host, int port, String nickname, int numPlayers) throws Exception {

    }

    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {

    }

    @Override
    public void skipTurn() throws Exception {

    }

    @Override
    public void disconnect() throws Exception {

    }
}
