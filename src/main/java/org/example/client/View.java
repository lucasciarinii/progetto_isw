package org.example.client;

import org.example.model.cards.Card;
import org.example.model.enums.Era;
import org.example.model.enums.GamePhase;
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
    private List<String> turnOrder;

    // Board
    private List<Card> topRow;
    private List<Card> bottomRow;
    private List<OfferTileSnapshot> offerTrack;
    private List<TurnSlotSnapshot> turnOrderSlots;

    // Players
    private List<PlayerSnapshot> players;

    // Game over
    private List<String> winners;

    public View() {
        this.turnOrder = new ArrayList<>();
        this.topRow = new ArrayList<>();
        this.bottomRow = new ArrayList<>();
        this.offerTrack = new ArrayList<>();
        this.turnOrderSlots = new ArrayList<>();
        this.players = new ArrayList<>();
        this.winners = new ArrayList<>();
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

    private void display() {
        System.out.println("\n========================================");
        System.out.println("===           GAME UPDATE            ===");
        System.out.println("========================================");
        System.out.println("Round: " + currentRound + " | Era: " + currentEra + " | Phase: " + currentPhase);
        System.out.println("Current Player: " + currentPlayerNickname);
        System.out.println("----------------------------------------");
        
        System.out.println("BOARD:");
        System.out.println("Top Row: " + topRow);
        System.out.println("Bottom Row: " + bottomRow);
        System.out.println("Offer Track: " + offerTrack);
        System.out.println("Turn Slots: " + turnOrderSlots);
        System.out.println("----------------------------------------");

        System.out.println("PLAYERS:");
        for (PlayerSnapshot p : players) {
            System.out.println("- " + p.getNickname() + " | Points: " + p.getPoints() + " | Food: " + p.getFood());
        }

        if (!winners.isEmpty()) {
            System.out.println("****************************************");
            System.out.println("GAME OVER! Winners: " + winners);
            System.out.println("****************************************");
        }
        System.out.println("========================================\n");
    }
}
