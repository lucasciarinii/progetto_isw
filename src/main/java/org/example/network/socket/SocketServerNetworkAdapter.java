package org.example.network.socket;

import org.example.network.ServerNetworkAdapter;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ServerController;

public class SocketServerNetworkAdapter implements ServerNetworkAdapter {

    private final ServerController controller;

    public SocketServerNetworkAdapter(ServerController controller) {
        this.controller = controller;
    }


    @Override
    public void start(int port) throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception {

    }

    @Override
    public void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception {

    }

    @Override
    public void sendError(String nickname, String errorMessage) throws Exception {

    }

    @Override
    public void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception {

    }

    @Override
    public void sendShutdown(String nickname) throws Exception {

    }
}
