package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;


public class NetworkAdapterFactory {

    // Client adapter
    public static ClientNetworkAdapter createClientAdapter(
            CommunicationProtocol protocol,
            ClientController controller) {
        return switch (protocol) {
            case RMI -> new RMIClientNetworkAdapter(controller);
            case SOCKET -> new SocketClientNetworkAdapter(controller);
        };
    }
}