package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;


/**
 * Factory for client network adapters based on the selected protocol.
 */
public class NetworkAdapterFactory {

    /**
     * Creates a client adapter for the chosen communication protocol.
     *
     * @param protocol   the communication protocol to use (RMI or Socket)
     * @param controller the client controller bound to the adapter
     * @return the corresponding client network adapter
     */
    public static ClientNetworkAdapter createClientAdapter(
            CommunicationProtocol protocol,
            ClientController controller) {
        return switch (protocol) {
            case RMI -> new RMIClientNetworkAdapter(controller);
            case SOCKET -> new SocketClientNetworkAdapter(controller);
        };
    }
}