package org.example.client.rmi;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;
import org.example.network.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;
import org.example.server.rmi.RMIClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/*?
    It's like an ANSWERING MACHINE: answers to RMI calls from the server,
    transforms them into method calls on the GameEventListener
    and passes them to the ClientController, which will update the view accordingly.

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
    public void receiveError(String errorMessage, GamePhase phase) throws RemoteException {
        listener.onError(errorMessage, phase);
    }

    @Override
    public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
        listener.onLobbyUpdate(update);
    }

    @Override
    public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
        listener.onRankingUpdate(rankingUpdate);
    }

    @Override
    public void receiveShutdown() throws RemoteException {
        listener.onShutdown();
    }

}
