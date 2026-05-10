package org.example.network.rmi;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
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
public class RMIGameServerImpl extends UnicastRemoteObject implements RMIGameServer, LobbyReadyListener {

    private final LobbyController lobby;
    private ServerController serverController; // null finché la lobby non è piena

    public RMIGameServerImpl() throws RemoteException {
        super();
        this.lobby = new LobbyController(this);
    }

    // Called by LobbyController when the lobby is full and the game can start
    @Override
    public void onLobbyReady(ServerController serverController) {
        this.serverController = serverController;
        System.out.println("[SERVER] Lobby full, game started!");
    }

    @Override
    public void register(String nickname, int numPlayers, RMIClientCallback callback)
            throws RemoteException {
        try {
            lobby.registerPlayer(nickname, numPlayers, callback);
        } catch (Exception e) {
            throw new RemoteException("Registration Error: " + e.getMessage());
        }
    }

    @Override
    public void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException {
        checkGameStarted();
        serverController.placeTotemOnOfferTile(nickname, tilePosition);
    }

    @Override
    public void offerTileAction(String nickname, String cards) throws RemoteException {
        checkGameStarted();
        serverController.offerTileAction(nickname, cards);
    }

    @Override
    public void skipTurn(String nickname) throws RemoteException {
        checkGameStarted();
        serverController.skipTurn(nickname);
    }

    private void checkGameStarted() throws RemoteException {
        if (serverController == null) {
            throw new RemoteException("Match is not started yet.");
        }
    }

    //! CLIENT CONNECTION: this methods sends messages to client

    public void sendLobbyUpdateToClient(String nickname, LobbyUpdateMessage update) throws RemoteException {
        RMIClientCallback callback = lobby.getCallbackByNickname(nickname);
        if (callback == null) {
            throw new RemoteException("Client not found: " + nickname);
        }
        callback.receiveLobbyUpdate(update);
    }

    public void sendGameStateUpdateToClient(String nickname, GameStateUpdateMessage update) throws RemoteException {
        checkGameStarted();
        ClientConnection connection = serverController.getClientConnectionByNickname(nickname);
        try {
            connection.sendGameStateUpdate(update);
        } catch (Exception e) {
            throw new RemoteException("Failed to send game state: " + e.getMessage());
        }
    }

    public void sendErrorToClient(String nickname, String errorMessage) throws RemoteException {
        checkGameStarted();
        ClientConnection connection = serverController.getClientConnectionByNickname(nickname);
        try {
            connection.sendError(errorMessage, serverController.getCurrentPhase());
        } catch (Exception e) {
            throw new RemoteException("Failed to send error: " + e.getMessage());
        }
    }

    public void sendRankingUpdateToClient(String nickname, RankingUpdateMessage update) throws RemoteException {
        checkGameStarted();
        ClientConnection connection = serverController.getClientConnectionByNickname(nickname);
        try {
            connection.sendRankingUpdate(update);
        } catch (Exception e) {
            throw new RemoteException("Failed to send ranking update: " + e.getMessage());
        }
    }

    public void sendShutdownToClient(String nickname) throws RemoteException {
        checkGameStarted();
        ClientConnection connection = serverController.getClientConnectionByNickname(nickname);
        try {
            connection.sendShutdown();
        } catch (Exception e) {
            throw new RemoteException("Failed to send shutdown: " + e.getMessage());
        }
    }

    //! SERVER STARTUP: Start RMI Registry and registers the server ---------------------------------------------------------------------------
    public static void startServer() throws Exception {
        // Forces RMI to use localhost instead of network board IP
        System.setProperty("java.rmi.server.hostname", "localhost");

        RMIGameServerImpl server = new RMIGameServerImpl();

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