package org.example.network.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.ClientConnection;
import org.example.server.model.enums.GamePhase;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class SocketClientConnection implements ClientConnection {

    private final PrintWriter out;
    private final ObjectMapper mapper = new ObjectMapper();

    public SocketClientConnection(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void sendGameStateUpdate(GameStateUpdateMessage update) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "GAME_STATE_UPDATE");
        msg.put("data", update);
        out.println(mapper.writeValueAsString(msg));
    }

    @Override
    public void sendError(String errorMessage, GamePhase phase) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "ERROR");
        msg.put("message", errorMessage);
        msg.put("phase", phase);
        out.println(mapper.writeValueAsString(msg));
    }

    @Override
    public void sendRankingUpdate(RankingUpdateMessage rankingUpdate) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "RANKING_UPDATE");
        msg.put("data", rankingUpdate);
        out.println(mapper.writeValueAsString(msg));
    }

    @Override
    public void sendLobbyUpdate(LobbyUpdateMessage update) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "LOBBY_UPDATE");
        msg.put("data", update);
        out.println(mapper.writeValueAsString(msg));
    }

    @Override
    public void sendShutdown() throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event", "SHUTDOWN");
        out.println(mapper.writeValueAsString(msg));
    }
}