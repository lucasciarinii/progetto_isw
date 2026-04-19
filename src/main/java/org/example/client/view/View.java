package org.example.client.view;

import org.example.server.model.cards.Card;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.GamePhase;
import org.example.network.GameStateUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.network.Snapshots.TurnSlotSnapshot;

import java.util.ArrayList;
import java.util.List;

public class View {
    private int currentRound;
    private Era currentEra;
    private GamePhase currentPhase;
    private String currentPlayerNickname;
    private List<String> turnOrder = new ArrayList<>();

    // Board
    private List<Card> topRow = new ArrayList<>();
    private List<Card> bottomRow = new ArrayList<>();
    private List<OfferTileSnapshot> offerTrack = new ArrayList<>();
    private List<TurnSlotSnapshot> turnOrderSlots = new ArrayList<>();

    // Players
    private List<PlayerSnapshot> players = new ArrayList<>();

    // Game over
    private List<String> winners = new ArrayList<>();

    public View() {
    }

    public List<OfferTileSnapshot> getOfferTrack() {
        return offerTrack;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void update(GameStateUpdateMessage message) {
        this.currentRound = message.getCurrentRound();
        this.currentEra = message.getCurrentEra();
        this.currentPhase = message.getCurrentPhase();
        this.currentPlayerNickname = message.getCurrentPlayerNickname();
        this.turnOrder = message.getTurnOrder();
        this.topRow = message.getTopRow();
        this.bottomRow = message.getBottomRow();
        this.offerTrack = message.getOfferTrack();
        this.turnOrderSlots = message.getTurnOrderSlots();
        this.players = message.getPlayers();
        this.winners = message.getWinners();
        
        display();
    }

    public void display() {
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

        System.out.println("ROUND: " + currentRound + " | ERA: " + currentEra + " | PHASE: " + currentPhase + " | CURRENT PLAYER: " + currentPlayerNickname);
        System.out.println("\nPLAYERS ----------------------------------------");
        for (PlayerSnapshot p : players) {
            System.out.println("- " + p.getNickname() + " | Points: " + p.getPoints() + " | Food: " + p.getFood());
            p.printAllCards();
        }

        System.out.println("\n\nTURN ORDER SLOTS ----------------------------------------");
        turnOrderSlots.forEach(System.out::println);
        System.out.println("\nOFFER TRACK ----------------------------------------");
        offerTrack.forEach(System.out::print);
        System.out.println("\n\nTOP ROW ----------------------------------------");
        topRow.forEach(System.out::print);
        System.out.println("\n\nBOTTOM ROW ----------------------------------------");
        bottomRow.forEach(System.out::print);


        if (!winners.isEmpty()) {
            System.out.println("****************************************");
            System.out.println("GAME OVER! Winners: " + winners);
            System.out.println("****************************************");
        }
        System.out.println("========================================\n");
    }

    public void displayWaiting(String currentPlayerNickname) {
        System.out.println("[WAIT] It's " + currentPlayerNickname + " turn...");
    }

    public void displayError(String error) {
        System.out.println("\n[ERROR] " + error + "\n");
    }

    public static void clearScreen() {
        System.out.println("\n".repeat(50));
    }

}
