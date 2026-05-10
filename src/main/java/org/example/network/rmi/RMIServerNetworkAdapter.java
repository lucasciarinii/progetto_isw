package org.example.network.rmi;

import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ServerController;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

public class RMIServerNetworkAdapter implements ServerNetworkAdapter {

    private final ServerController controller;
    private RMIGameServerImpl rmiServer;

    public RMIServerNetworkAdapter(ServerController controller) {
        this.controller = controller;
    }


    @Override
    public void start(int port) throws Exception {
        System.setProperty("java.rmi.server.hostname", "localhost");

        rmiServer = new RMIGameServerImpl();

        try {
            LocateRegistry.createRegistry(1099);
        } catch (ExportException e) {
            System.out.println("[SERVER] RMI registry already exists.");
        }

        java.rmi.Naming.rebind("//localhost/GameServer", rmiServer);
        System.out.println("[SERVER] RMI ready on port 1099. Waiting for players...");
    }

    @Override
    public void stop() throws Exception {
        // RMI doesn't explicitly require a stop
    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendLobbyUpdateToClient(nickname, update);
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendGameStateUpdateToClient(nickname, update);
    }

    @Override
    public void sendError(String nickname, String errorMessage) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendErrorToClient(nickname, errorMessage);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendRankingUpdateToClient(nickname, update);
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendShutdownToClient(nickname);
    }
}
