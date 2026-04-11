package org.example.server.rmi;

import org.example.server.ServerController;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/*? Concrete Implementation of the GameServer interface -> server actually implements the "contract" of what it can do for clients.
    Receives RMI calls from clients and delegates them to the ServerController, which contains the actual game logic. */

public class GameServerImpl extends UnicastRemoteObject implements GameServer {
    private final ServerController controller;

    public GameServerImpl(ServerController controller) throws RemoteException {
        super();
        this.controller = controller;
    }

    @Override
    public void register(String nickname, ClientCallback callback) throws RemoteException {
        RMIClientConnection connection = new RMIClientConnection(callback);
        controller.registerClient(connection, nickname);
    }

    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        RMIClientConnection connection = controller.getConnectionByNickname(nickname); // utility method to find the connection associated with the nickname
        controller.placeTotemOnOfferTile(connection, tilePosition);
    }

    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        RMIClientConnection connection = controller.getConnectionByNickname(nickname);
        controller.offerTileAction(connection, cards);
    }

    //! SERVER STARTUP: Start RMI Registry and registers the server ---------------------------------------------------------------------------
    public static void startServer(List<Player> players) throws Exception {
        Match match = new Match(players);
        ServerController controller = new ServerController(match);
        GameServerImpl server = new GameServerImpl(controller);

        LocateRegistry.createRegistry(1099);
        java.rmi.Naming.rebind("GameServer", server);
        System.out.println("RMI server ready on 1099 port.");
    }
}
