package org.example.client.rmi;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.server.rmi.RMIClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/*? Client-side implementation of the ClientCallback interface.
    Server calls the methods of this class to send updates and errors to the client.
    Note: this class DOES NOT KNOW anything about the view, delegates the work to the ClientController

 */
public class RMIClientCallbackImpl extends UnicastRemoteObject implements RMIClientCallback {

    private final GameEventListener listener;

    public RMIClientCallbackImpl(GameEventListener listener) throws RemoteException {
        super();
        this.listener = listener;
    }

    @Override
    public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
        listener.onUpdate(update);
    }

    @Override
    public void receiveError(String errorMessage) throws RemoteException {
        listener.onError(errorMessage);
    }

    @Override
    public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
        listener.onLobbyUpdate(update);
    }

}
