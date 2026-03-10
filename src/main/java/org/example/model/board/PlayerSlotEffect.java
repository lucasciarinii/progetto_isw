package org.example.model.board;
import org.example.model.enums.TurnOrderEffect;

public class PlayerSlotEffect extends PlayerSlot {
    private final TurnOrderEffect effect;

    public PlayerSlotEffect(String playerName, TurnOrderEffect effect) {
        super(playerName);
        this.effect = effect;
    }

    public TurnOrderEffect getEffect() {
        return effect;
    }
}
