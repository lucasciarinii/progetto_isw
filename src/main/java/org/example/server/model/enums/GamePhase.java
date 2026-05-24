package org.example.server.model.enums;

import org.example.server.model.interfaces.GamePhaseInterface;
import org.example.server.model.match.GameState;

/**
 * Phases of the game lifecycle with transition logic.
 */
public enum GamePhase implements GamePhaseInterface {
    /** Waiting for players to join and setup. */
    LOBBY {
        @Override
        public GamePhase next(GameState state) {
            return PLACE_TOTEMS;
        }
    },

    /** Players place totems on the board. */
    PLACE_TOTEMS {
        @Override
        public GamePhase next(GameState state) {
            return PLAYER_TURN;
        }
    },

    /** Main player turn actions. */
    PLAYER_TURN {
        @Override
        public GamePhase next(GameState state) {
            return EVENTS_RESOLVE;
        }
    },

    /** Resolve any pending events. */
    EVENTS_RESOLVE {
        @Override
        public GamePhase next(GameState state) {
            return END_ROUND;
        }
    },

    /** End-of-round cleanup and round advancement. */
    END_ROUND {
        @Override
        public GamePhase next(GameState state) {
            if (state.getCurrentRound() == 10) {
                return END_GAME;
            } else {
                state.advanceRound();
                return PLACE_TOTEMS;
            }
        }
    },

    /** Final scoring preparation. */
    END_GAME {
        @Override
        public GamePhase next(GameState state) {
            return GAME_OVER;
        }
    },

    /** Terminal state after the game ends. */
    GAME_OVER {
        @Override
        public GamePhase next(GameState state) {
            return GAME_OVER; // no next phase, game is over
        }
    };


    @Override
    public String toString() {
        return switch(this) {
            case LOBBY -> "LOBBY";
            case PLACE_TOTEMS -> "PLACE_TOTEMS";
            case PLAYER_TURN -> "PLAYER_TURN";
            case EVENTS_RESOLVE -> "EVENTS_RESOLVE";
            case END_ROUND -> "END_ROUND";
            case END_GAME -> "END_GAME";
            case GAME_OVER -> "GAME_OVER";
        };
    }
}
