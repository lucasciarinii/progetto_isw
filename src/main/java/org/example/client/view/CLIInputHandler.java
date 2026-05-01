package org.example.client.view;

import org.example.client.ClientController;
import org.example.network.GameStateUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CLIInputHandler {
    private final ClientController controller;
    private final Scanner scanner = new Scanner(System.in);

    public CLIInputHandler(ClientController controller) {
        this.controller = controller;
    }

    public void promptForAction(GamePhase phase) {
        // New Thread to avoid blocking the RMI thread while waiting for user input
        new Thread(() -> {
            switch (phase) {
                case PLACE_TOTEMS -> handlePlaceTotem();
                case PLAYER_TURN  -> handleOfferTileAction();
                default          -> {} // automatic phases, no inputs
            }
        }).start();
    }

    // -------------------------------------------------------

    private void handlePlaceTotem() {
        while (true) {
            System.out.print(">>> Choose the offer tile (1-" + controller.getView().getOfferTrack().size()  + ") : ");
            String input = scanner.nextLine().trim();
            try {
                int pos = Integer.parseInt(input);
                if (pos < 1 || pos > controller.getView().getOfferTrack().size()) {
                    System.out.println("[!] Invalid position. Insert a number between 1 and " + controller.getView().getOfferTrack().size());
                    continue;
                }
                controller.placeTotemOnOfferTile(pos);
                break; // command sent, exit from the loop
            } catch (NumberFormatException e) {
                System.out.println("[!] Insert a valid number.");
            } catch (Exception e) {
                // Error from server (invalid move, not your turn, ecc.)
                System.out.println("[ERROR] " + e.getMessage());
                break; // do not try again — server will send a new update
            }
        }
    }

    private void handleOfferTileAction() {
        GameStateUpdateMessage state = controller.getView().getLastUpdate();

        // Get the offer effect of the tile where the current player's totem is placed
        OfferEffect effect = state.getOfferTrack().stream()
                .filter(tile -> controller.getNickname().equals(tile.getOccupantNickname()))
                .map(OfferTileSnapshot::getOfferEffect)
                .findFirst()
                .orElse(null);

        // Get the player snapshot of the current player
        PlayerSnapshot player = state.getPlayers().stream()
                .filter(p -> p.getNickname().equals(controller.getNickname()))
                .findFirst()
                .orElseThrow();

        int fromBottom = 0;
        int fromTop    = 0;

        switch (effect) {
            case D -> fromBottom = (int) Math.min(1, countPickable(state.getBottomRow(), player));
            case DD -> fromBottom = (int) Math.min(2, countPickable(state.getBottomRow(), player));
            case U -> fromTop = (int) Math.min(1, countPickable(state.getTopRow(), player));
            case UU -> fromTop = (int) Math.min(2, countPickable(state.getTopRow(), player));
            case DU -> {
                fromBottom = (int) Math.min(1, countPickable(state.getBottomRow(), player));
                fromTop = (int) Math.min(1, countPickable(state.getTopRow(), player));
            }
            case DUU -> {
                fromBottom = (int) Math.min(1, countPickable(state.getBottomRow(), player));
                fromTop = (int) Math.min(2, countPickable(state.getTopRow(), player));
            }
            case FOOD -> {
                // no ID to ask
                try { controller.offerTileAction(""); } catch (Exception e) { System.out.println("[ERROR] " + e.getMessage()); }
                return;
            }
        }

        // ASK for IDs (in order)
        List<Integer> ids = new ArrayList<>();
        ids.addAll(askIds("bottom row", state.getBottomRow(), fromBottom));
        ids.addAll(askIds("top row", state.getTopRow(), fromTop));

        String payload = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            controller.offerTileAction(payload);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }



private List<Integer> askIds(String rowName, List<Card> row, int count) {
    List<Integer> ids = new ArrayList<>();
    for (int i = 0; i < count; i++) {
        while (true) {
            System.out.print(">>> Card " + (i + 1) + " from " + rowName + " (ID): ");
            try {
                int id = Integer.parseInt(scanner.nextLine().trim());
                ids.add(id);
                break;
            } catch (NumberFormatException e) {
                System.out.println("[!] Insert a valid number.");
            }
        }
    }
    return ids;
}

    private long countPickable(List<Card> row, PlayerSnapshot player) {
        return row.stream()
                .filter(c -> c.isCharacter() ||
                        (c.isBuilding() &&
                                ((BuildingCard) c).getFoodCost() <= player.getFood()
                                        + player.getDiscountOnBuilding()))
                .count();
    }

    public void warnExit() {
        System.out.println("GAME ENDED, THANKS FOR PLAYING...");
        System.exit(0);
    }
}