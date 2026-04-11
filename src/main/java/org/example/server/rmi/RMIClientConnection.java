package org.example.server.rmi;

import org.example.server.ClientConnection;
import org.example.network.GameStateUpdateMessage;

//? The "adapter": it let RMI talk with ServerController
//? RMI Implementation of ClientConnection interface. This is the object that the server will use to send updates and errors to the client, by calling the methods of the ClientCallback interface implemented by the client.
public class RMIClientConnection implements ClientConnection {
    private final ClientCallback callback;

    public RMIClientConnection(ClientCallback callback) {
        this.callback = callback;
    }

    @Override
    public void sendUpdate(GameStateUpdateMessage update) throws Exception {
        callback.receiveUpdate(update);
    }

    @Override
    public void sendError(String errorMessage) throws Exception {
        callback.receiveError(errorMessage);
    }

}
