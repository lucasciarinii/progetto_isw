package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;

public class NetworkAdapterFactory {

    // Create the adapter for the client
    public static ClientNetworkAdapter createClientAdapter(
            CommunicationProtocol protocol,
            ClientController controller) {

        return switch(protocol) {
            case RMI -> new RMIClientNetworkAdapter(controller);
            case SOCKET ->  new SocketClientNetworkAdapter(controller);
        };
    }

    // Create the adapter for the server
    public static ServerNetworkAdapter createServerAdapter(
            CommunicationProtocol protocol
    ) {

        return switch (protocol) {
            case RMI -> new RMIServerNetworkAdapter();
            case SOCKET ->  new SocketServerNetworkAdapter();
        };
    }

}
