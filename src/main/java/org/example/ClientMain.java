package org.example;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Use:");
            System.out.println("  java -jar mesos-client.jar tui [rmi|socket] [port] <host>");
            System.out.println("  java -jar mesos-client.jar gui [rmi|socket] [port] <host>");
            return;
        }

        String mode = args.length > 0 ? args[0] : "tui";
        org.example.network.CommunicationProtocol protocol = App.parseProtocol(args.length > 1 ? args[1] : "rmi");
        int port = args.length > 2 ? Integer.parseInt(args[2]) : App.defaultPort(protocol);
        String host = args.length > 3 ? args[3] : null;

        if (host == null || host.isBlank()) {
            System.out.println("Missing server host. Example: java -jar mesos-client.jar tui rmi 1099 192.168.1.10");
            return;
        }

        App.startClient(host, mode, protocol, port);
    }
}
