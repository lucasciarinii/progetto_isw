package org.example.client.rmi;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote client-side interface exposed to the server for callbacks.
 * The server invokes these methods to push updates and errors to the client.
 */
public interface RMIClientCallback extends Remote {

    /**
     * Receives a game state snapshot update.
     *
     * @param update the game state update
     * @throws RemoteException if the callback fails
     */
    void receiveUpdate(GameStateUpdateMessage update) throws RemoteException;

    /**
     * Receives an error message from the server.
     *
     * @param errorMessage the error description
     * @param phase        the related game phase
     * @throws RemoteException if the callback fails
     */
    void receiveError(String errorMessage, GamePhase phase) throws RemoteException;

    /**
     * Receives an updated lobby snapshot.
     *
     * @param update the lobby update
     * @throws RemoteException if the callback fails
     */
    void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException;

    /**
     * Receives a ranking update.
     *
     * @param rankingUpdate the ranking update
     * @throws RemoteException if the callback fails
     */
    void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException;

    /**
     * Receives a RoundFlow card request.
     *
     * @throws RemoteException if the callback fails
     */
    void receiveRoundFlowCardRequest() throws RemoteException;

    /**
     * Receives a shutdown notification from the server.
     *
     * @throws RemoteException if the callback fails
     */
    void receiveShutdown() throws RemoteException;

}
