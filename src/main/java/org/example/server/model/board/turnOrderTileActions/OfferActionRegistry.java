package org.example.server.model.board.turnOrderTileActions;

import org.example.server.model.enums.OfferEffect;

import java.util.EnumMap;
import java.util.Map;

public class OfferActionRegistry {

    private final Map<OfferEffect, OfferActionStrategy> strategies = new EnumMap<>(OfferEffect.class);

    public OfferActionRegistry() {
        strategies.put(OfferEffect.FOOD, new FoodActionStrategy());
        strategies.put(OfferEffect.D, new DActionStrategy());
        strategies.put(OfferEffect.U, new UActionStrategy());
        strategies.put(OfferEffect.DD, new DDActionStrategy());
        strategies.put(OfferEffect.DU, new DUActionStrategy());
        strategies.put(OfferEffect.UU, new UUActionStrategy());
        strategies.put(OfferEffect.DUU, new DUUActionStrategy());
    }

    public OfferActionStrategy getStrategy(OfferEffect effect) {
        OfferActionStrategy strategy = strategies.get(effect);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown or unsupported OfferEffect");
        }
        return strategy;
    }
}