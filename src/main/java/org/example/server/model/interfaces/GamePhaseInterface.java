package org.example.server.model.interfaces;

import org.example.server.model.enums.GamePhase;
import org.example.server.model.match.GameState;

public interface GamePhaseInterface {
    GamePhase next(GameState state);
}
