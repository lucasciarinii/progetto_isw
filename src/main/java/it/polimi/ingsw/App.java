package it.polimi.ingsw;

import it.polimi.ingsw.client.view.GUI.GUILauncher;
import it.polimi.ingsw.client.view.TUI.TUILauncher;
import it.polimi.ingsw.network.ClientNetworkAdapter;
import it.polimi.ingsw.network.CommunicationProtocol;
import it.polimi.ingsw.network.HybridServerNetworkAdapter;
import it.polimi.ingsw.server.database.DatabaseInitializer;
import it.polimi.ingsw.server.ServerLogger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Scanner;

public class App {

    // kept for local debug, IDE runs, or a single-jar launcher.
    // Production packaging uses ClientMain/ServerMain, but this remains handy for quick testing.
    static void main(String[] args) throws Exception {
        System.out.println("App started with args: " + java.util.Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos.jar server");
            System.out.println("  java -jar mesos.jar client tui [rmi|socket] <host>");
            System.out.println("  java -jar mesos.jar client gui [rmi|socket] <host>");
            System.out.println("Env:");
            System.out.println("  SERVER_HOST to force the server bind address (LAN IP)");
            return;
        }

        // Entry-point router for the single-jar launcher: delegate to server or client flow.
        // The real work is in startServer/startClient to keep the logic reusable.
        switch (args[0]) {
            case "server" -> startServer();
            case "client" -> {
                String mode = args.length > 1 ? args[1] : "tui";
                CommunicationProtocol protocol = parseProtocol(args.length > 2 ? args[2] : "rmi");
                String host = args.length > 3 ? args[3] : null;

                if (host == null || host.isBlank()) {
                    System.out.println("Missing server host. Example: java -jar mesos.jar client tui rmi 192.168.1.10");
                    return;
                }

                startClient(host, mode, protocol);
            }
            default -> System.out.println("Argument not recognized: " + args[0]);
        }
    }

    // =========================================================================
    // SERVER
    // =========================================================================

    // Server entry point: initialize DB, resolve bind host, and start network adapters.
    public static void startServer() throws Exception {
        // If the database doesn't exist, create it with the right schema
        DatabaseInitializer.ensureDatabase();

        String serverHost = resolveServerHost();
        System.setProperty("mesos.server.host", serverHost);
        HybridServerNetworkAdapter adapter = new HybridServerNetworkAdapter();
        adapter.start();
        ServerLogger.server("Server started on host " + serverHost + ". Press ENTER to shut down.");
        new Scanner(System.in).nextLine();
        adapter.stop();
        ServerLogger.server("Server OFF.");
        System.exit(0);
    }

    // Shared helper used by client launchers to map CLI strings to protocol.
    public static CommunicationProtocol parseProtocol(String raw) {
        if ("socket".equalsIgnoreCase(raw)) {
            return CommunicationProtocol.SOCKET;
        }
        return CommunicationProtocol.RMI;
    }

    // Shared helper to provide protocol-specific default ports for clients.
    public static int defaultPort(CommunicationProtocol protocol) {
        return protocol == CommunicationProtocol.SOCKET ? ClientNetworkAdapter.SOCKET_PORT : ClientNetworkAdapter.RMI_PORT;
    }

    // Resolve the LAN IP for the server so remote clients can reach it.
    public static String resolveServerHost() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to localhost below if detection fails.
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return "127.0.0.1";
        }
    }

    // =========================================================================
    // CLIENT
    // =========================================================================

    // Client entry point: choose GUI/TUI and start the chosen transport.
    public static void startClient(String host, String mode, CommunicationProtocol protocol) {
        switch (mode) {
            case "tui" -> TUILauncher.launchTuiClient(host, protocol);
            case "gui" -> GUILauncher.launchGuiClient(host, protocol);
            default -> System.out.println("Client mode not recognized: " + mode + ". Use 'tui' or 'gui'.");
        }
    }
}