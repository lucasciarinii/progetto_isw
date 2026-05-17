package org.example.client.view.TUI;

import org.example.client.ClientController;
import org.example.client.view.UIHandler;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.server.database.RankingEntry;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.enums.OfferEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TUIHandler implements UIHandler {
    private ClientController controller;
    private final Scanner scanner = new Scanner(System.in);
    private GameStateUpdateMessage lastUpdate;

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    //! TUI EVENTS -----------------------------------------------
    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        if (update.isGameStarting()) {
            System.out.println("Match is starting!");
        } else {
            System.out.println("In lobby: " + update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players");
            System.out.println("Connected: " + update.getPlayerNicknames());
        }
    }

    @Override
    public void onGameStateUpdate(GameStateUpdateMessage update) {
        this.lastUpdate = update;
        display(update);
    }

    @Override
    public void onError(String errorMessage, GamePhase currentPhase) {
        System.out.println("\n[ERROR] " + errorMessage + "\n");
        promptForAction(currentPhase);
    }

    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingUpdate) {
        displayRanking(rankingUpdate.getRanking(), rankingUpdate.getPlayerRankPosition());
    }

    @Override
    public void onRoundFlowCardRequest() {
        //TODO
        handleRoundFlowCardRequest();
    }

    @Override
    public void onShutdown() {
        System.out.println("GAME ENDED, THANKS FOR PLAYING...");
        System.exit(0);
    }

    @Override
    public void promptForAction(GamePhase phase) {
        new Thread(() -> {
            switch (phase) {
                case PLACE_TOTEMS -> handlePlaceTotem();
                case PLAYER_TURN  -> handleOfferTileAction();
                default           -> {}
            }
        }).start();
    }

    @Override
    public void displayNoCardsPickable() {
        System.out.println("\n[INFO] No cards pickable, turn skipped. Press ENTER to continue...\n");
        scanner.nextLine();
    }

    @Override
    public void displayWaiting(String currentPlayerNickname) {
        System.out.println("[WAIT] It's " + currentPlayerNickname + "'s turn...");
    }

    private void handlePlaceTotem() {
        while (true) {
            System.out.print(">>> Choose offer tile (1-" + lastUpdate.getOfferTrack().size() + "): ");
            try {
                int pos = Integer.parseInt(scanner.nextLine().trim());
                if (pos < 1 || pos > lastUpdate.getOfferTrack().size()) {
                    System.out.println("[!] Invalid position.");
                    continue;
                }
                controller.placeTotemOnOfferTile(pos);
                break;
            } catch (NumberFormatException e) {
                System.out.println("[!] Insert a valid number.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
                break;
            }
        }
    }

    private void handleOfferTileAction() {
        OfferEffect effect = lastUpdate.getOfferTrack().stream()
                .filter(tile -> controller.getNickname().equals(tile.getOccupantNickname()))
                .map(OfferTileSnapshot::getOfferEffect)
                .findFirst()
                .orElse(null);

        PlayerSnapshot player = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(controller.getNickname()))
                .findFirst()
                .orElseThrow();

        int fromBottom = 0;
        int fromTop    = 0;

        switch (effect) {
            case D   -> fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), player));
            case DD  -> fromBottom = (int) Math.min(2, countPickable(lastUpdate.getBottomRow(), player));
            case U   -> fromTop    = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), player));
            case UU  -> fromTop    = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), player));
            case DU  -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), player));
                fromTop    = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), player));
            }
            case DUU -> {
                fromBottom = (int) Math.min(1, countPickable(lastUpdate.getBottomRow(), player));
                fromTop    = (int) Math.min(2, countPickable(lastUpdate.getTopRow(), player));
            }
            case FOOD -> {
                try { controller.offerTileAction(""); } catch (Exception e) { System.out.println("[ERROR] " + e.getMessage()); }
                return;
            }
            case null -> { throw new IllegalStateException("Current player's totem is not on any offer tile!"); }
        }

        List<Integer> ids = new ArrayList<>();
        ids.addAll(askIds("bottom row", fromBottom));
        ids.addAll(askIds("top row",    fromTop));

        String payload = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            controller.offerTileAction(payload);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }


    private void handleRoundFlowCardRequest() {

        PlayerSnapshot player = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(controller.getNickname()))
                .findFirst()
                .orElseThrow();


        int fromTop = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), player));

        List<Integer> ids = new ArrayList<>();

        ids.addAll(askIds("top row", fromTop));

        String payload = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            controller.roundFlowCardRequest(payload);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private List<Integer> askIds(String rowName, int count) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            while (true) {
                System.out.print(">>> Card " + (i + 1) + " from " + rowName + " (ID): ");
                try {
                    ids.add(Integer.parseInt(scanner.nextLine().trim()));
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

    private void display(GameStateUpdateMessage update) {
        clearScreen();
        String logo = """
                $$\\      $$\\ $$$$$$$$\\  $$$$$$\\   $$$$$$\\   $$$$$$\\ \s
                $$$\\    $$$ |$$  _____|$$  __$$\\ $$  __$$\\ $$  __$$\\\s
                $$$$\\  $$$$ |$$ |      $$ /  \\__|$$ /  $$ |$$ /  \\__|
                $$\\$$\\$$ $$ |$$$$$\\    \\$$$$$$\\  $$ |  $$ |\\$$$$$$\\ \s
                $$ \\$$$  $$ |$$  __|    \\____$$\\ $$ |  $$ | \\____$$\\\s
                $$ |\\$  /$$ |$$ |      $$\\   $$ |$$ |  $$ |$$\\   $$ |
                $$ | \\_/ $$ |$$$$$$$$\\ \\$$$$$$  | $$$$$$  |\\$$$$$$  |
                \\__|     \\__|\\________| \\______/  \\______/  \\______/\s
                """;

        System.out.println(logo);

        System.out.println("ROUND: " + update.getCurrentRound() + " | ERA: " + update.getCurrentEra() + " | PHASE: " + update.getCurrentPhase() + " | CURRENT PLAYER: " + update.getCurrentPlayerNickname());
        System.out.println("\nPLAYERS ----------------------------------------");
        for (PlayerSnapshot p : update.getPlayers()) {
            System.out.println("- " + p.getNickname() + " | Points: " + p.getPoints() + " | Food: " + p.getFood());
            p.printAllCards();
        }

        System.out.println("\n\nTURN ORDER SLOTS ----------------------------------------");
        update.getTurnOrderSlots().forEach(System.out::println);
        System.out.println("\nOFFER TRACK ----------------------------------------");
        update.getOfferTrack().forEach(System.out::print);
        System.out.println("\n\nTOP ROW ----------------------------------------");
        update.getTopRow().forEach(System.out::print);
        System.out.println("\n\nBOTTOM ROW ----------------------------------------");
        update.getBottomRow().forEach(System.out::print);


        if (!update.getWinners().isEmpty()) {
            System.out.println("****************************************");
            System.out.println("GAME OVER! Winners: " + update.getWinners());
            System.out.println("****************************************");
        }
        System.out.println("========================================\n");
    }

    private void displayRanking(List<RankingEntry> ranking, int playerRankPosition) {
        System.out.println("\n========================================");
        System.out.println("         GLOBAL RANKING");
        System.out.println("========================================");

        if (ranking.isEmpty()) {
            System.out.println("No players in ranking yet.");
        } else {
            int position = 1;
            for (RankingEntry entry : ranking) {
                System.out.printf("%2d. %-20s Wins: %d | Avg score: %.1f%n", position, entry.getNickname(), entry.getWins(), entry.getAvgScore());
                position++;
            }
        }

        System.out.println("----------------------------------------");
        if (playerRankPosition == -1) {
            System.out.println("You are not in the ranking yet (no wins).");
        } else {
            System.out.println("Your position: #" + playerRankPosition);
        }
        System.out.println("========================================\n");    }

    public static void clearScreen() {
        System.out.println("\n".repeat(50));
    }


}
