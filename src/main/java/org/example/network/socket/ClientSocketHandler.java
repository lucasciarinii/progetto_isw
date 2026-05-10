package org.example.network.socket;

import java.net.Socket;
import java.util.Map;

public class ClientSocketHandler implements Runnable {

    private Socket socket;
    private SocketServerNetworkAdapter server;
    private final Map<String, ClientSocketHandler> connectedClients;


    public ClientSocketHandler(Socket socket, SocketServerNetworkAdapter server,
                               Map<String, ClientSocketHandler> connectedClients) {
        this.socket = socket;
        this.server = server;
        this.connectedClients = connectedClients;
    }


    public void close() {

    }

    public void send(String msg) {

    }





    @Override
    public void run() {

    }
}
