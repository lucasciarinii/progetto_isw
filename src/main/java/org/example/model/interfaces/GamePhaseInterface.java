package org.example.model.interfaces;

import org.example.model.enums.GamePhase;
import org.example.model.match.GameState;

public interface GamePhaseInterface {
    GamePhase next(GameState state);
}
