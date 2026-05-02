package org.example;

import org.example.client.ClientController;
import org.example.client.view.GUI.GUILauncher;
import org.example.client.view.TUI.TUIHandler;
import org.example.server.rmi.RMIGameServerImpl;

import java.util.Scanner;

public class App {

    static void main(String[] args) throws Exception {
        System.out.println("App started with args: " + java.util.Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos.jar server");
            System.out.println("  java -jar mesos.jar client tui <host>");
            System.out.println("  java -jar mesos.jar client gui <host>");
            return;
        }

        switch (args[0]) {
            case "server" -> startServer();
            case "client" -> {
                String mode = args.length > 1 ? args[1] : "tui";
                String host = args.length > 2 ? args[2] : "localhost";
                startClient(host, mode);
            }
            default -> System.out.println("Argument not recognized: " + args[0]);
        }
    }

    // =========================================================================
    // SERVER
    // =========================================================================


    private static void startServer() throws Exception {
        RMIGameServerImpl.startServer();
        // Server RMI runs in background on daemon thread
        System.out.println("Server Started. Press ENTER to shut down.");
        new Scanner(System.in).nextLine();
        System.out.println("Server OFF.");
        System.exit(0);
    }

    // =========================================================================
    // CLIENT
    // =========================================================================

    private static void startClient(String host, String mode) {
        switch (mode) {
            case "tui" -> startClientTui(host);
            case "gui" -> startClientGui(host);
            default -> System.out.println("Client mode not recognized: " + mode + ". Use 'tui' or 'gui'.");
        }
    }


    private static void startClientTui(String host) {
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


    private static void startClientGui(String host) {
        GUILauncher.launchGuiClient(host);
    }
}