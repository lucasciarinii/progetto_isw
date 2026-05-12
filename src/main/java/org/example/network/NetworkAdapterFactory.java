package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;

import java.rmi.RemoteException;

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

    // Server adapter
    public static ServerNetworkAdapter createServerAdapter() {
        try {
            return new HybridServerNetworkAdapter();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create hybrid server: " + e.getMessage(), e);
        }
    }
}
