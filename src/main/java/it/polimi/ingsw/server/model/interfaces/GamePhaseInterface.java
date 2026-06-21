package it.polimi.ingsw.server.model.interfaces;

import it.polimi.ingsw.server.model.enums.GamePhase;
import it.polimi.ingsw.server.model.match.GameState;

/**
 * Defines how a game phase transitions to the next phase.
 */
public interface GamePhaseInterface {
    /**
     * Computes the next phase given the current game state.
     *
     * @param state current game state
     * @return the next game phase
     */
    GamePhase next(GameState state);
}
