package org.example.server.model.enums;

/**
 * Effects granted by the turn order track.
 */
public enum TurnOrderEffect {
    /** No effect. */
    EMPTY,
    /** Gain 1 food. */
    FOOD1,
    /** Gain 2 food. */
    FOOD2,
    /** Gain 3 food. */
    FOOD3,
    /** Apply the malus: -1 food / -2 points. */
    MALUS
}
