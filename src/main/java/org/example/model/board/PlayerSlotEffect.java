package org.example.Model.board;
import org.example.Model.enums.TurnOrderEffect;

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
