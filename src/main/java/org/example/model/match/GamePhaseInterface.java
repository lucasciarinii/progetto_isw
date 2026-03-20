package org.example.model.match;

import org.example.model.enums.GamePhase;

public interface GamePhaseInterface {
    GamePhase next(GameState state);
}
