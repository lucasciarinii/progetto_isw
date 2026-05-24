package org.example.client.view.TUI;

import org.example.client.ClientController;
import org.example.network.CommunicationProtocol;

import java.util.Scanner;

public class TUILauncher {

    private static final String LOBBY_ART =
            " _      ____  ____  ____ ___  _ \n" +
                    "/ \\  /|/  _ \\/  _ \\/  _ \\\\  \\// \n" +
                    "| |  ||| / \\|| | //| | // \\  /  \n" +
                    "| |/\\||| \\_/|| |_\\\\| |_\\\\ / /   \n" +
                    "\\_/  \\|\\____/\\____/\\____//_/    \n";

    public static void launchTuiClient(String host, CommunicationProtocol protocol, int port) {
        Scanner scanner = new Scanner(System.in);
        int selectedOption = -1;

        System.out.println(LOBBY_ART);

        // MENU LOOP
        while (selectedOption != 1 && selectedOption != 2) {
            System.out.println("Choose an action:");
            System.out.println("1. Join an existing Lobby");
            System.out.println("2. Create a new Lobby");
            System.out.print("> ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                selectedOption = 1;
            } else if (input.equals("2")) {
                selectedOption = 2;
            } else {
                System.out.println("Invalid choice. Please type 1 or 2.\n");
            }
        }

        // REDIRECTION
        if (selectedOption == 2) {
            handleCreateLobby(scanner, host, port, protocol);
        } else {
            handleJoinLobby(scanner, host, port, protocol);
        }
    }

    private static void handleCreateLobby(Scanner scanner, String host, int port, CommunicationProtocol protocol) {
        System.out.println("\n=== CREATE A NEW LOBBY ===");

        System.out.print("Insert your nickname: ");
        String nickname = scanner.nextLine().trim();

        System.out.print("Insert number of players (2-5): ");
        int numPlayers;
        try {
            numPlayers = Integer.parseInt(scanner.nextLine().trim());
            if (numPlayers < 2 || numPlayers > 5) throw new IllegalArgumentException();
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to 4 players.");
            numPlayers = 4;
        }

        startClientCreateLobby(host, port, nickname, numPlayers, protocol);
    }

    private static void handleJoinLobby(Scanner scanner, String host, int port, CommunicationProtocol protocol) {
        System.out.println("\n=== JOIN AN EXISTING LOBBY ===");

        while (true) {
            System.out.print("Insert game code: ");
            String code = scanner.nextLine().trim();

            System.out.print("Insert your nickname: ");
            String nickname = scanner.nextLine().trim();

            if (startClientJoinLobby(host, port, nickname, code, protocol)) {
                break;
            }

            System.out.println("Join failed. Please try a different code or nickname.\n");
        }
    }

    private static boolean startClientJoinLobby(String host, int port, String nickname, String gameID, CommunicationProtocol protocol) {
        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            clientController.joinLobbyAndConnect(host, port, gameID, protocol);
            return true;
        } catch (Exception e) {
            System.out.println("\nJoin error: " + e.getMessage());
            return false;
        }
    }

    private static void startClientCreateLobby(String host, int port, String nickname, int numPlayers, CommunicationProtocol protocol) {
        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            clientController.createLobbyAndConnect(host, port, numPlayers, protocol);
        } catch (Exception e) {
            System.out.println("\nImpossible to connect to server at address: " + host);
            System.out.println("Ensure server is active and the address is correct.");
        }
    }

}