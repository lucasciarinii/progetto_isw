package org.example.model.board;

import org.example.model.enums.TurnOrderEffect;
import org.example.model.match.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TurnOrderTile {
    private final List<PlayerSlot> slots;

    public TurnOrderTile(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");
        // Random order is already set in Match.init() so we can use progressively the players in the list to assign them to the slots

        // Initialize player slots based on the number of players
        switch (players.size()) {
            case 2 -> slots = List.of(new PlayerSlotEffect(players.get(0).getNickname(), TurnOrderEffect.FOOD1), new PlayerSlotEffect(players.get(1).getNickname(), TurnOrderEffect.MALUS));
            case 3 -> slots = List.of(new PlayerSlotEffect(players.get(0).getNickname(), TurnOrderEffect.FOOD2), new PlayerSlot(players.get(1).getNickname()), new PlayerSlotEffect(players.get(2).getNickname(), TurnOrderEffect.MALUS));
            case 4 -> slots = List.of(new PlayerSlotEffect(players.get(0).getNickname(), TurnOrderEffect.FOOD2), new PlayerSlotEffect(players.get(1).getNickname(), TurnOrderEffect.FOOD1), new PlayerSlot(players.get(2).getNickname()), new PlayerSlotEffect(players.get(3).getNickname(), TurnOrderEffect.MALUS));
            case 5 -> slots = List.of(new PlayerSlotEffect(players.get(0).getNickname(), TurnOrderEffect.FOOD3), new PlayerSlotEffect(players.get(1).getNickname(), TurnOrderEffect.FOOD1), new PlayerSlot(players.get(2).getNickname()), new PlayerSlot(players.get(3).getNickname()), new PlayerSlotEffect(players.get(4).getNickname(), TurnOrderEffect.MALUS));
            default -> throw new IllegalArgumentException("Invalid list of players");
        }
    }

    public List<PlayerSlot> getSlots(){return slots;}
}
