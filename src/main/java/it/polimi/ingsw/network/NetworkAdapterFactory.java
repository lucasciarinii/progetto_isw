package it.polimi.ingsw.network;

import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.network.rmi.RMIClientNetworkAdapter;
import it.polimi.ingsw.network.socket.SocketClientNetworkAdapter;


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