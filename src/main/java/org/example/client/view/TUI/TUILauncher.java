package org.example.client.view.TUI;

import org.example.client.ClientController;

import java.util.Scanner;

public final class TUILauncher {

    private TUILauncher() {}

    public static void launchTuiClient(String host) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Insert your nickname: ");
        String nickname = scanner.nextLine().trim();

        System.out.print("Insert number of players: (2-5) " + "[ignore if you are not the first to connect]: ");
        int numPlayers = Integer.parseInt(scanner.nextLine().trim());

        try {
            TUIHandler tui = new TUIHandler();
            ClientController clientController = new ClientController(nickname, tui);
            tui.setController(clientController);
            clientController.connect(host, numPlayers);
        }
        catch (Exception e) {
            System.out.println("Impossible to connect to server RIM to address: " + host);
            System.out.println("Ensure server is active and the address is correct.");
        }
    }
}

