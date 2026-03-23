package org.example.model.board;
import org.example.model.enums.TurnOrderEffect;

import java.util.Objects;

public class PlayerSlotEffect extends PlayerSlot {
    private final TurnOrderEffect effect;

    public PlayerSlotEffect(String playerName, TurnOrderEffect effect) {
        super(playerName);
        this.effect = Objects.requireNonNull(effect, "Effect cannot be null");
    }

    public TurnOrderEffect getEffect() {
        return effect;
    }
}
