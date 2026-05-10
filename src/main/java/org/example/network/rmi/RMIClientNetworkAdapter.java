package org.example.network.rmi;

import org.example.client.ClientController;
import org.example.client.rmi.RMIClientCallbackImpl;
import org.example.network.ClientNetworkAdapter;
import org.example.server.rmi.RMIGameServer;

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
    public void connect(String host, int port, String nickname, int numPlayers) throws Exception {
        System.setProperty("java.rmi.server.hostname", "localhost");
        this.nickname = nickname;

        server = (RMIGameServer) Naming.lookup("rmi://" + host + "/GameServer");
        RMIClientCallbackImpl callback = new RMIClientCallbackImpl(controller);
        server.register(nickname, port, callback);
    }

    @Override
    public void placeTotemOnOfferTile(int tilePosition) throws Exception {
        server.placeTotemOnOfferTile(nickname, tilePosition);
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
