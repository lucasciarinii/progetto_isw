package org.example.model.board;

import java.util.List;

public class TurnOrderTile {
    private List<PlayerSlotEffect> slots;

    public TurnOrderTile(List<PlayerSlotEffect> slots) {
        this.slots = slots;
    }

    public List<PlayerSlotEffect> getSlots(){return slots;}
}
