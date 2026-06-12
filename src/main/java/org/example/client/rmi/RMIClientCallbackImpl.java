package org.example.client.rmi;

import org.example.client.GameEventListener;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.rmi.RMIGameServer;
import org.example.server.model.enums.GamePhase;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Client-side RMI callback implementation that forwards events to a listener.
 * It does not know about the view; it delegates to the GameEventListener.
 */
public class RMIClientCallbackImpl extends UnicastRemoteObject implements RMIClientCallback {

    private final GameEventListener listener;

    // Used for ping-pong
    private final RMIGameServer server;
    private final String nickname;
    private final AtomicLong lastPingAt;

    /**
     * Creates a callback bound to a game event listener.
     *
     * @param listener the listener that handles client events
     * @throws RemoteException if the remote object cannot be exported
     */
    public RMIClientCallbackImpl(GameEventListener listener, RMIGameServer server, String nickname, AtomicLong lastPingAt) throws RemoteException {
        super();
        this.listener = listener;
        this.server = server;
        this.nickname = nickname;
        this.lastPingAt = lastPingAt;
    }


    /**
     * Forwards a game state update to the listener.
     */
    @Override
    public void receiveUpdate(GameStateUpdateMessage update) throws RemoteException {
        listener.onUpdate(update);
    }

    /**
     * Forwards an error to the listener.
     */
    @Override
    public void receiveError(String errorMessage, GamePhase phase) throws RemoteException {
        listener.onError(errorMessage, phase);
    }

    /**
     * Forwards a lobby update to the listener.
     */
    @Override
    public void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException {
        listener.onLobbyUpdate(update);
    }

    /**
     * Forwards a ranking update to the listener.
     */
    @Override
    public void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException {
        listener.onRankingUpdate(rankingUpdate);
    }

    /**
     * Forwards a RoundFlow request to the listener.
     */
    @Override
    public void receiveRoundFlowCardRequest() {
        listener.onRoundFlowCardRequest();
    }

    /**
     * Forwards a shutdown notification to the listener.
     */
    @Override
    public void receiveShutdown() throws RemoteException {
        listener.onShutdown();
    }

    /**
     * Method to communicate to the server that the client is alive (ping pong)
     */
    @Override
    public void receivePing() throws RemoteException {
        lastPingAt.set(System.currentTimeMillis());
        server.pong(nickname);
    }


}
