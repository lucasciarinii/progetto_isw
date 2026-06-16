package it.polimi.ingsw.client.view.TUI;

import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.client.view.UIHandler;
import it.polimi.ingsw.network.messages.GameStateUpdateMessage;
import it.polimi.ingsw.network.messages.LobbyUpdateMessage;
import it.polimi.ingsw.network.messages.RankingUpdateMessage;
import it.polimi.ingsw.network.snapshots.OfferTileSnapshot;
import it.polimi.ingsw.network.snapshots.PlayerSnapshot;
import it.polimi.ingsw.server.database.RankingEntry;
import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.enums.GamePhase;
import it.polimi.ingsw.server.model.enums.OfferEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Text-based UI handler for TUI VIEW that renders updates and collects player input.
 * It forwards player choices to ClientController actions.
 */
public class TUIHandler implements UIHandler {
    private ClientController controller;
    private final Scanner scanner = new Scanner(System.in);
    private GameStateUpdateMessage lastUpdate;
    private String gameID;
    private boolean lobbyRetryEnabled = false;

    // The queue: threads handler that uses a SINGLE thread to execute tasks -> ensures that only ONE thread at a time interacts with System.in.
    private final ExecutorService inputExecutor = newSingleThreadExecutor();
    // Thread-safe flag to check if the user is currently interacting with a prompt
    private final AtomicBoolean inputBusy = new AtomicBoolean(false);

    /**
     * Binds the client controller used to send player actions.
     *
     * @param controller the client controller
     */
    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public void setLobbyRetryEnabled(boolean enabled) {
        this.lobbyRetryEnabled = enabled;
    }

    @Override
    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    //! TUI EVENTS -----------------------------------------------
    @Override
    public void onLobbyUpdate(LobbyUpdateMessage update) {
        if (update.isGameStarting()) {
            System.out.println("Match is starting!");
        } else {
            System.out.println("GAME CODE: " + update.getGameID());
            System.out.println("In lobby: " + update.getConnectedPlayers() + "/" + update.getRequiredPlayers() + " players");
            System.out.println("Connected: " + update.getPlayerNicknames());
        }
    }

    /**
     * Renders the game state and caches the latest update.
     */
    @Override
    public void onGameStateUpdate(GameStateUpdateMessage update) {
        this.lastUpdate = update;
        display(update);
    }

    /**
     * Displays an error and re-prompts if needed.
     */
    @Override
    public void onError(String errorMessage, GamePhase currentPhase) {
        System.out.println("\n[ERROR] " + errorMessage + "\n");
        if (currentPhase == GamePhase.LOBBY) {
            if (lobbyRetryEnabled) {
                inputExecutor.submit(() -> {
                    if (!inputBusy.compareAndSet(false, true)) {
                        return;
                    }
                        handleLobbyRetry();
                        inputBusy.set(false);
                });
            }
            return;
        }
        promptForAction(currentPhase);
    }

    private void handleLobbyRetry() {
        while (true) {
            System.out.print("Insert game code: ");
            String code = scanner.nextLine().trim();
            System.out.print("Insert your nickname: ");
            String nickname = scanner.nextLine().trim();
            try {
                controller.setNickname(nickname);
                controller.joinLobby(code);
                break;
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    @Override
    public void onRankingUpdate(RankingUpdateMessage rankingUpdate) {
        displayRanking(rankingUpdate.getRanking(), rankingUpdate.getPlayerRankPosition());
    }

    /**
     * Asks the user to resolve the RoundFlow card request.
     */
    @Override
    public void onRoundFlowCardRequest() {
        inputExecutor.submit(this::handleRoundFlowCardRequest);
    }

    /**
     * Displays the shutdown notice.
     */
    @Override
    public void onShutdown() {
        System.out.println("GAME ENDED, THANKS FOR PLAYING...");
        System.exit(0);
    }

    /**
     * Prompts the user for an action based on the current phase.
     */
    @Override
    public void promptForAction(GamePhase phase) {
        // Transfer the TUI input handling to a separate thread to prevent blocking other threads (that want to read from System.in).
        inputExecutor.submit(() -> {
            /* Atomic Check-and-Set: If the user is already typing in another prompt,
               discard this overlapping request to prevent duplicate or interleaved prompts. */
            if (!inputBusy.compareAndSet(false, true)) {
                return;
            }
            try {
                switch (phase) {
                    case PLACE_TOTEMS -> handlePlaceTotem();
                    case PLAYER_TURN  -> handleOfferTileAction();
                    case GAME_ABORTED -> handleGameAborted();
                    default           -> {}
                }
            } finally {
                /* ALWAYS release the lock, ensuring the TUI
                   doesn't freeze permanently if an exception occurs during input. */
                inputBusy.set(false);
            }
        });
    }



    /**
     * Informs the user that no cards are pickable and the turn was skipped.
     */
    @Override
    public void displayNoCardsPickable() {
        System.out.println("\n[INFO] No cards pickable, turn skipped. Press ENTER to continue...\n");
        scanner.nextLine();
    }

    /**
     * Displays that the client is waiting for the current player.
     */
    @Override
    public void displayWaiting(String currentPlayerNickname) {
        System.out.println("[WAIT] " + currentPlayerNickname + "'s turn...");
    }

    /**
     * Displays that another player is resolving RoundFlow.
     */
    @Override
    public void displayRoundFlowWaiting(String currentPlayerNickname) {
        System.out.println("[WAIT] " + currentPlayerNickname + " is picking an extra card (RoundFlow building)...");
    }

    /**
     * Reads the target offer tile and calls ClientController.placeTotemOnOfferTile.
     */
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

    /**
     * Builds the card selection payload and calls ClientController.offerTileAction.
     */
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
            case null ->  throw new IllegalStateException("Current player's totem is not on any offer tile!");
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


    /**
     * Builds the RoundFlow payload and calls ClientController.roundFlowCardRequest.
     */
    private void handleRoundFlowCardRequest() {
        System.out.println("[INFO] RoundFlow active: pick an extra card from the top row.");
        PlayerSnapshot player = lastUpdate.getPlayers().stream()
                .filter(p -> p.getNickname().equals(controller.getNickname()))
                .findFirst()
                .orElseThrow();


        int fromTop = (int) Math.min(1, countPickable(lastUpdate.getTopRow(), player));

        List<Integer> ids = new ArrayList<>(askIds("top row", fromTop));

        String payload = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            controller.roundFlowCardRequest(payload);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void handleGameAborted() {
        System.out.println("\nPress ENTER to shutdown...");
        new Scanner(System.in).nextLine();
        System.exit(0);
    }

    /**
     * Prompts for card IDs from a specific row.
     */
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

    /**
     * Counts the cards a player can pick from a row.
     */
    private long countPickable(List<Card> row, PlayerSnapshot player) {
        return row.stream()
                .filter(c -> c.isCharacter() ||
                        (c.isBuilding() &&
                                ((BuildingCard) c).getFoodCost() <= player.getFood()
                                        + player.getDiscountOnBuilding()))
                .count();
    }

    /**
     * Prints the full game state to the console.
     */
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
        System.out.println("\nLOBBY: " + gameID + "\n");

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

    /**
     * Prints the ranking table to the console.
     */
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

    /**
     * Clears the console output
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


}
