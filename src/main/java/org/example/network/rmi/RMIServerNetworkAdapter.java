package org.example.network.rmi;

import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

public class RMIServerNetworkAdapter implements ServerNetworkAdapter {

    private RMIGameServerImpl rmiServer;

    public RMIServerNetworkAdapter() {
    }


    @Override
    public void start(int port) throws Exception {
        System.setProperty("java.rmi.server.hostname", "localhost");

        rmiServer = new RMIGameServerImpl();

        try {
            LocateRegistry.createRegistry(port);
        } catch (ExportException e) {
            System.out.println("[SERVER] RMI registry already exists.");
        }

        java.rmi.Naming.rebind("//localhost:" + port + "/GameServer", rmiServer);
        System.out.println("[SERVER] RMI ready on port " + port + ". Waiting for players...");
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
        rmiServer.sendLobbyUpdate(nickname, update);
    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendGameStateUpdate(nickname, update);
    }

    @Override
    public void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendError(nickname, errorMessage, phase);
    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendRankingUpdate(nickname, update);
    }

    @Override
    public void sendShutdown(String nickname) throws Exception {
        if (rmiServer == null) {
            throw new IllegalStateException("RMI server not started");
        }
        rmiServer.sendShutdown(nickname);
    }
}
