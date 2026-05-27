package org.example;

import org.example.client.view.GUI.GUILauncher;
import org.example.client.view.TUI.TUILauncher;
import org.example.network.CommunicationProtocol;
import org.example.network.HybridServerNetworkAdapter;
import org.example.server.database.DatabaseInitializer;
import org.example.server.ServerLogger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Scanner;

public class App {

    static void main(String[] args) throws Exception {
        System.out.println("App started with args: " + java.util.Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos.jar server");
            System.out.println("  java -jar mesos.jar client tui [rmi|socket] [port] <host>");
            System.out.println("  java -jar mesos.jar client gui [rmi|socket] [port] <host>");
            System.out.println("Env:");
            System.out.println("  SERVER_HOST to force the server bind address (LAN IP)");
            return;
        }

        // Quick notes:
        // - The server binds RMI on the LAN IP (auto-detect or SERVER_HOST) so clients on the same Wi-Fi can reach it.
        // - Clients must pass the server IP as <host> (works for both GUI/TUI and RMI/SOCKET).
        // - RMI clients set java.rmi.server.hostname to their local IP for callbacks.
        // - Socket server binds on all interfaces by default (no extra host config needed).
        //
        // Find server IP:
        // - Linux:   ip -4 addr | grep -E "inet " | grep -v 127.0.0.1
        // - macOS:   ipconfig getifaddr en0   (try en1 if empty)
        // - Windows: Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -ne "127.0.0.1" }

        switch (args[0]) {
            case "server" -> {
                startServer();
            }
            case "client" -> {
                String mode = args.length > 1 ? args[1] : "tui";
                CommunicationProtocol protocol = parseProtocol(args.length > 2 ? args[2] : "rmi");
                int port = args.length > 3 ? Integer.parseInt(args[3]) : defaultPort(protocol);
                String host = args.length > 4 ? args[4] : null;

                if (host == null || host.isBlank()) {
                    System.out.println("Missing server host. Example: java -jar mesos.jar client tui rmi 1099 192.168.1.10");
                    return;
                }

                startClient(host, mode, protocol, port);
            }
            default -> System.out.println("Argument not recognized: " + args[0]);
        }
    }

    // =========================================================================
    // SERVER
    // =========================================================================

    private static void startServer() throws Exception {
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

    private static CommunicationProtocol parseProtocol(String raw) {
        if ("socket".equalsIgnoreCase(raw)) {
            return CommunicationProtocol.SOCKET;
        }
        return CommunicationProtocol.RMI;
    }

    private static int defaultPort(CommunicationProtocol protocol) {
        return protocol == CommunicationProtocol.SOCKET ? 9999 : 1099;
    }

    private static String resolveServerHost() {
        String forced = System.getenv("SERVER_HOST");
        if (forced != null && !forced.isBlank()) {
            return forced.trim();
        }
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

    private static void startClient(String host, String mode, CommunicationProtocol protocol, int port) {
        switch (mode) {
            case "tui" -> TUILauncher.launchTuiClient(host, protocol, port);
            case "gui" -> GUILauncher.launchGuiClient(host, protocol, port);
            default -> System.out.println("Client mode not recognized: " + mode + ". Use 'tui' or 'gui'.");
        }
    }
}