package org.example;

import org.example.client.view.GUI.GUILauncher;
import org.example.client.view.TUI.TUILauncher;
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
        TUILauncher.launchTuiClient(host);
    }


    private static void startClientGui(String host) {
        GUILauncher.launchGuiClient(host);
    }
}