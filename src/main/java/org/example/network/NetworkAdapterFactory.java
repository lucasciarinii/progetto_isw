package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;

import java.rmi.RemoteException;

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
            case RMI -> {
                try {
                    yield new RMIServerNetworkAdapter();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
            case SOCKET ->  new SocketServerNetworkAdapter();
        };
    }

}
