package org.example;

import org.example.client.ClientController;
import org.example.server.rmi.RMIGameServerImpl;

import java.util.Scanner;

public class App {

    public static void main(String[] args) throws Exception {
        System.out.println("App avviata con args: " + java.util.Arrays.toString(args));
        if (args.length == 0) {
            System.out.println("Uso:");
            System.out.println("  java -jar mesos.jar server");
            System.out.println("  java -jar mesos.jar client <host>");
            return;
        }

        switch (args[0]) {
            case "server" -> startServer();
            case "client" -> {
                String host = args.length > 1 ? args[1] : "localhost";
                startClient(host);
            }
            default -> System.out.println("Argomento non riconosciuto: " + args[0]);
        }
    }

    // =========================================================================
    // SERVER
    // =========================================================================

    private static void startServer() throws Exception {
        RMIGameServerImpl.startServer();
        // Il server RMI gira in background su thread daemon —
        // questo loop mantiene il processo vivo
        System.out.println("Server avviato. Premi INVIO per spegnere.");
        new Scanner(System.in).nextLine();
        System.out.println("Server spento.");
        System.exit(0);
    }

    // =========================================================================
    // CLIENT
    // =========================================================================

    private static void startClient(String host) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Inserisci il tuo nickname: ");
        String nickname = scanner.nextLine().trim();

        System.out.print("Inserisci il numero di giocatori (2-5) " +
                "[ignorato se non sei il primo a connetterti]: ");
        int numPlayers = Integer.parseInt(scanner.nextLine().trim());

        ClientController client = new ClientController(nickname);
        client.connect(host, numPlayers);

        // Mantiene il client vivo in attesa di aggiornamenti
        System.out.println("In attesa... (premi INVIO per disconnetterti)");
        scanner.nextLine();
        System.exit(0);
    }
}