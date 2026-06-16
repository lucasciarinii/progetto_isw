package org.example;

public class ClientMain {
    static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos-client.jar tui [rmi|socket] <host>");
            System.out.println("  java -jar mesos-client.jar gui [rmi|socket] <host>");
            return;
        }

        String mode = args[0];
        org.example.network.CommunicationProtocol protocol = App.parseProtocol(args.length > 1 ? args[1] : "rmi");
        String host = args.length > 2 ? args[2] : null;

        if (host == null || host.isBlank()) {
            System.out.println("Missing server host. Example: java -jar mesos-client.jar tui rmi 192.168.1.10");
            return;
        }

        App.startClient(host, mode, protocol);
    }
}
