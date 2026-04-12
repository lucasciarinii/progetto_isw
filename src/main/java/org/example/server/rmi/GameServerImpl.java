package org.example.server.rmi;

import org.example.server.ClientConnection;
import org.example.server.LobbyController;
import org.example.server.LobbyReadyListener;
import org.example.server.ServerController;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/*? Concrete Implementation of the GameServer interface -> server actually implements the "contract" of what it can do for clients.
    Receives RMI calls from clients and delegates them to the ServerController, which contains the actual game logic. */
public class GameServerImpl extends UnicastRemoteObject implements GameServer, LobbyReadyListener {

    private final LobbyController lobby;
    private ServerController serverController; // null finché la lobby non è piena

    public GameServerImpl() throws RemoteException {
        super();
        this.lobby = new LobbyController(this);
    }

    // Called by LobbyController when the lobby is full and the game can start
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
        System.out.println("[SERVER] Lobby piena, partita avviata!");
    }

    @Override
    public void register(String nickname, int numPlayers, ClientCallback callback)
            throws RemoteException {
        try {
            lobby.registerPlayer(nickname, numPlayers, callback);
        } catch (Exception e) {
            throw new RemoteException("Errore registrazione: " + e.getMessage());
        }
    }

    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        checkGameStarted();
        ClientConnection conn = serverController.getConnectionByNickname(nickname);
        serverController.placeTotemOnOfferTile(conn, tilePosition);
    }

    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        checkGameStarted();
        ClientConnection conn = serverController.getConnectionByNickname(nickname);
        serverController.offerTileAction(conn, cards);
    }

    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }

    //! SERVER STARTUP: Start RMI Registry and registers the server ---------------------------------------------------------------------------
    public static void startServer() throws Exception {
        // Forces RMI to use localhost instead of network board IP
        System.setProperty("java.rmi.server.hostname", "localhost");

        GameServerImpl server = new GameServerImpl();

        // Try to create registry, if exists, reuse it
        try {
            LocateRegistry.createRegistry(1099);
        } catch (ExportException e) {
            System.out.println("[SERVER] Registry already exists, re-use it.");
        }

        java.rmi.Naming.rebind("//localhost/GameServer", server);
        System.out.println("[SERVER] RMI ready on 1099 port. Waiting for players...");
    }
}