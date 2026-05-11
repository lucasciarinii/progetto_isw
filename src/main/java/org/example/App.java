package org.example;

import org.example.client.view.GUI.GUILauncher;
import org.example.client.view.TUI.TUILauncher;
import org.example.network.CommunicationProtocol;
import org.example.network.NetworkAdapterFactory;
import org.example.network.ServerNetworkAdapter;

import java.util.Scanner;

public class App {

    static void main(String[] args) throws Exception {
        System.out.println("App started with args: " + java.util.Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos.jar server [rmi|socket] [port]");
            System.out.println("  java -jar mesos.jar client tui [rmi|socket] [port] <host>");
            System.out.println("  java -jar mesos.jar client gui [rmi|socket] [port] <host>");
            return;
        }

        switch (args[0]) {
            case "server" -> {
                CommunicationProtocol protocol = parseProtocol(args.length > 1 ? args[1] : "rmi");
                int port = args.length > 2 ? Integer.parseInt(args[2]) : defaultPort(protocol);

                startServer(protocol, port);
            }
            case "client" -> {
                String mode = args.length > 1 ? args[1] : "tui";
                CommunicationProtocol protocol = parseProtocol(args.length > 2 ? args[2] : "rmi");
                int port = args.length > 3 ? Integer.parseInt(args[3]) : defaultPort(protocol);
                String host = args.length > 4 ? args[4] : "localhost";

                startClient(host, mode, protocol, port);
            }
            default -> System.out.println("Argument not recognized: " + args[0]);
        }
    }

    // =========================================================================
    // SERVER
    // =========================================================================

    private static void startServer(CommunicationProtocol protocol, int port) throws Exception {
        ServerNetworkAdapter adapter = NetworkAdapterFactory.createServerAdapter(protocol);
        adapter.start(port);
        System.out.println("Server Started. Press ENTER to shut down.");

        new Scanner(System.in).nextLine();
        adapter.stop();
        System.out.println("Server OFF.");
        System.exit(0);
    }

    private static CommunicationProtocol parseProtocol(String raw) {
        if ("socket".equalsIgnoreCase(raw)) {
            return CommunicationProtocol.SOCKET;
        }
        return CommunicationProtocol.RMI;
    }

    private static int defaultPort(CommunicationProtocol protocol) {
        return protocol == CommunicationProtocol.SOCKET ? 9999 : 1099;
    }

    // =========================================================================
    // CLIENT
    // =========================================================================

    private static void startClient(String host, String mode, CommunicationProtocol protocol, int port) {
        switch (mode) {
            case "tui" -> TUILauncher.launchTuiClient(host, protocol, port);
            case "gui" -> GUILauncher.launchGuiClient(host, protocol, port);
            default -> System.out.println("Client mode not recognized: " + mode + ". Use 'tui' or 'gui'.");
        }
    }
}