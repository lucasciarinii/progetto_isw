package it.polimi.ingsw.client.view;

import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.server.model.enums.GamePhase;

/**
 * UI abstraction for client views (TUI/GUI) to receive updates and prompts.
 */
public interface UIHandler {

    /** @param update the lobby update to render */
    void onLobbyUpdate(LobbyUpdateMessage update);

    /** @param update the game state update to render */
    void onGameStateUpdate(GameStateUpdateMessage update);

    /** @param errorMessage the error description
     *  @param currentPhase the phase where the error occurred */
    void onError(String errorMessage, GamePhase currentPhase);

    /** @param rankingMessage the ranking update to render */
    void onRankingUpdate(RankingUpdateMessage rankingMessage);

    /** Called when the server requests a RoundFlow choice. */
    void onRoundFlowCardRequest();

    /** Called when the server notifies shutdown. */
    void onShutdown();

    /** @param phase the current phase to prompt actions for */
    void promptForAction(GamePhase phase);

    /** Informs the user that no cards are pickable and the turn is skipped. */
    void displayNoCardsPickable();

    /** @param currentPlayerNickname the nickname of the active player */
    void displayWaiting(String currentPlayerNickname);

    /** @param currentPlayerNickname the nickname of the RoundFlow player */
    void displayRoundFlowWaiting(String currentPlayerNickname);

    /** Used to set the gameID */
    void setGameID(String gameID);

}
