package org.example.network;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

public interface ServerNotifier {
    void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception;

    void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception;

    void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception;

    void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception;

    void sendRoundFlowCardRequest(String nickname) throws Exception;

    void sendShutdown(String nickname) throws Exception;
}

