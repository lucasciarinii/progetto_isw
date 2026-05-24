package org.example.client.view.TUI;

import org.example.client.ClientController;
import org.example.network.CommunicationProtocol;

import java.util.Scanner;

/**
 * Entry point helper for starting the TUI client.
 */
public final class TUILauncher {

    private TUILauncher() {}

    /**
     * Prompts for connection info and starts a TUI client session.
     *
     * @param host     the server host
     * @param protocol the communication protocol
     * @param port     the server port
     */
    public static void launchTuiClient(String host, CommunicationProtocol protocol, int port) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Insert your nickname: ");
        String nickname = scanner.nextLine().trim();

        System.out.print("Insert number of players: (2-5) [ignore if you are not the first to connect]: ");
        int numPlayers = Integer.parseInt(scanner.nextLine().trim());

        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            clientController.connect(host, port, numPlayers, protocol);
        }
        catch (Exception e) {
            System.out.println("Impossible to connect to server at address: " + host);
            System.out.println("Ensure server is active and the address is correct.");
        }
    }
}
