package it.polimi.ingsw.client.view.TUI;

import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.network.CommunicationProtocol;

import java.util.Scanner;

/**
 * Entry point helper for starting the TUI client.
 */
public class TUILauncher {

    private static final String LOBBY_ART = """
                                \s
                    __  __  ______   _____   ____    _____\s
                   |  \\/  ||  ____| / ____| / __ \\  / ____|
                   | \\  / || |__   | (___  | |  | || (___ \s
                   | |\\/| ||  __|   \\___ \\ | |  | | \\___ \\\s
                   | |  | || |____  ____) || |__| | ____) |
                   |_|  |_||______||_____/  \\____/ |_____/\s
                                             \s
                       ~ Enter the Mesolithic world ~

       _______________________          _______________________
      |                       |        |                       |
      |    [1] JOIN LOBBY     |        |    [2] CREATE LOBBY   |
      |_______________________|        |_______________________|
""";

    /**
     * Prompts for connection info and starts a TUI client session.
     *
     * @param host     the server host
     * @param protocol the communication protocol
     */
    public static void launchTuiClient(String host, CommunicationProtocol protocol) {
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
            handleCreateLobby(scanner, host, protocol);
        } else {
            handleJoinLobby(scanner, host, protocol);
        }
    }

    /**
     * Prompts the user for nickname and player count, then starts the flow
     * to create a new lobby.
     *
     * @param scanner the scanner used to read user input
     * @param host the server host
     * @param protocol the communication protocol
     */
    private static void handleCreateLobby(Scanner scanner, String host, CommunicationProtocol protocol) {
        System.out.println("\n=== CREATE A NEW LOBBY ===");

        System.out.print("Insert your nickname: ");
        String nickname = scanner.nextLine().trim();

        int numPlayers = 0;
        while(numPlayers < 2 || numPlayers > 5) {
            System.out.print("Insert number of players (2-5): ");
            numPlayers = Integer.parseInt(scanner.nextLine().trim());
        }

        startClientCreateLobby(host, nickname, numPlayers, protocol);
    }

    /**
     * Prompts the user for lobby code and nickname, then attempts
     * to join an existing lobby until the operation succeeds.
     *
     * @param scanner the scanner used to read user input
     * @param host the server host
     * @param protocol the communication protocol
     */
    private static void handleJoinLobby(Scanner scanner, String host, CommunicationProtocol protocol) {
        System.out.println("\n=== JOIN AN EXISTING LOBBY ===");

        while (true) {
            System.out.print("Insert game code: ");
            String code = scanner.nextLine().trim();

            System.out.print("Insert your nickname: ");
            String nickname = scanner.nextLine().trim();

            if (startClientJoinLobby(host, nickname, code, protocol)) {
                break;
            }

            System.out.println("Join failed. Please try a different code or nickname.\n");
        }
    }

    /**
     * Creates the TUI client components and attempts to join an existing lobby.
     *
     * @param host the server host
     * @param nickname the player's nickname
     * @param gameID the identifier of the lobby to join
     * @param protocol the communication protocol
     * @return {@code true} if the join operation succeeds, {@code false} otherwise
     */
    private static boolean startClientJoinLobby(String host, String nickname, String gameID, CommunicationProtocol protocol) {
        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            tui.setLobbyRetryEnabled(protocol == CommunicationProtocol.SOCKET);
            clientController.joinLobbyAndConnect(host, gameID, protocol);
            return true;
        } catch (Exception e) {
            if (protocol == CommunicationProtocol.SOCKET) {
                System.out.println("\nJoin error: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Creates the TUI client components and starts a new lobby.
     *
     * @param host the server host
     * @param nickname the player's nickname
     * @param numPlayers the number of players required to start the lobby
     * @param protocol the communication protocol
     */
    private static void startClientCreateLobby(String host, String nickname, int numPlayers, CommunicationProtocol protocol) {
        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            clientController.createLobbyAndConnect(host, numPlayers, protocol);
        } catch (Exception e) {
            System.out.println("\nImpossible to connect to server at address: " + host);
            System.out.println("Ensure server is active and the address is correct.");
        }
    }

}