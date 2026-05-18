package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.enums.OfferEffect;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry that maps offer effects to their strategies.
 */
public class OfferActionRegistry {

    /** Map of effects to actions. */
    private final Map<OfferEffect, OfferActionStrategy> actions = new EnumMap<>(OfferEffect.class);

    /**
     * Registers all supported offer actions.
     */
    public OfferActionRegistry() {
        actions.put(OfferEffect.FOOD, new FoodActionStrategy());
        actions.put(OfferEffect.D, new DActionStrategy());
        actions.put(OfferEffect.U, new UActionStrategy());
        actions.put(OfferEffect.DD, new DDActionStrategy());
        actions.put(OfferEffect.DU, new DUActionStrategy());
        actions.put(OfferEffect.UU, new UUActionStrategy());
        actions.put(OfferEffect.DUU, new DUUActionStrategy());
    }

    /**
     * Returns the strategy for the given offer effect.
     *
     * @param effect offer effect
     * @return action strategy
     */
    public OfferActionStrategy getActionByEffect(OfferEffect effect) {
        OfferActionStrategy action = actions.get(effect);
        if (action == null) {
            throw new IllegalArgumentException("Unknown or unsupported OfferEffect");
        }
        return action;
    }
}