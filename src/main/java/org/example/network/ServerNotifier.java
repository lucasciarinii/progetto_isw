package org.example.network;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

/**
 * Sends server-to-client notifications for lobby, match, and shutdown events.
 */
public interface ServerNotifier {
    /**
     * Sends a lobby update to a specific player.
     *
     * @param nickname the target player's nickname
     * @param update   the lobby update payload
     * @throws Exception if the notification cannot be delivered
     */
    void sendLobbyUpdate(String nickname, LobbyUpdateMessage update) throws Exception;

    /**
     * Sends a game-state update to a specific player.
     *
     * @param nickname the target player's nickname
     * @param update   the game-state snapshot
     * @throws Exception if the notification cannot be delivered
     */
    void sendGameStateUpdate(String nickname, GameStateUpdateMessage update) throws Exception;

    /**
     * Sends an error message to a specific player.
     *
     * @param nickname     the target player's nickname
     * @param errorMessage the error description
     * @param phase        the game phase associated with the error
     * @throws Exception if the notification cannot be delivered
     */
    void sendError(String nickname, String errorMessage, GamePhase phase) throws Exception;

    /**
     * Sends a ranking update to a specific player.
     *
     * @param nickname the target player's nickname
     * @param update   the ranking update payload
     * @throws Exception if the notification cannot be delivered
     */
    void sendRankingUpdate(String nickname, RankingUpdateMessage update) throws Exception;

    /**
     * Asks the specified player to answer the RoundFlow card request.
     *
     * @param nickname the target player's nickname
     * @throws Exception if the notification cannot be delivered
     */
    void sendRoundFlowCardRequest(String nickname) throws Exception;

    /**
     * Sends a shutdown signal to a specific player.
     *
     * @param nickname the target player's nickname
     * @throws Exception if the notification cannot be delivered
     */
    void sendShutdown(String nickname) throws Exception;

    /**
     * Handles client disconnection by performing necessary cleanup and logging.
     *
     */
    void handleClientDisconnect(String nickname, String reason);
}

