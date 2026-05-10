package org.example.network;

import org.example.client.ClientController;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.rmi.RMIClientNetworkAdapter;
import org.example.network.rmi.RMIServerNetworkAdapter;
import org.example.network.socket.SocketClientNetworkAdapter;
import org.example.network.socket.SocketServerNetworkAdapter;
import org.example.server.ServerController;

public class NetworkAdapterFactory {


    // Create the adapter for the client
    public static ClientNetworkAdapter createClientAdapter(
            CommunicationProtocol protocol,
            ClientController controller) throws Exception {

        return switch(protocol) {
            case RMI -> new RMIClientNetworkAdapter(controller);
            case SOCKET ->  new SocketClientNetworkAdapter(controller);
        };
    }

    // Create the adapter for the server
    public static ServerNetworkAdapter createServerAdapter(
            CommunicationProtocol protocol,
            ServerController controller
    ) throws Exception {

        return switch (protocol) {
            case RMI -> new RMIServerNetworkAdapter(controller);
            case SOCKET ->  new SocketServerNetworkAdapter(controller);
        };
    }



}
