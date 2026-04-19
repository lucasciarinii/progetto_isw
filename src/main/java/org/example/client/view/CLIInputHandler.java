package org.example.client.view;

import org.example.client.ClientController;
import org.example.server.model.enums.GamePhase;

import java.util.Scanner;

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
        System.out.print(">>> Insert the number of cards to play (Ex: '1,2,3' or '' to play nothing): ");
        String input = scanner.nextLine().trim();
        try {
            controller.offerTileAction(input);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}