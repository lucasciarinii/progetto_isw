package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote server-side interface exposed via RMI.
 * Clients obtain a stub from the registry and invoke these methods to send actions.
 */
public interface RMIGameServer extends Remote {

    /* Create or join a game with the client callback used for server updates. */
    String createLobby(String nickname, int numPlayers, RMIClientCallback callback) throws Exception;

    void joinLobby(String nickname, String gameID, RMIClientCallback callback) throws Exception;

    /**
     * Places a totem on the selected offer tile.
     *
     * @param nickname     the player's nickname
     * @param tilePosition the offer tile index
     * @throws RemoteException if the remote call fails
     */
    void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException;

    /**
     * Resolves the offer tile action for the current player.
     *
     * @param nickname the player's nickname
     * @param cards    the serialized card selection
     * @throws RemoteException if the remote call fails
     */
    void offerTileAction(String nickname, String cards) throws RemoteException;

    /**
     * Resolves a RoundFlow card request for the current player.
     *
     * @param nickname the player's nickname
     * @param cards    the serialized card selection
     * @throws RemoteException if the remote call fails
     */
    void roundFlowCardRequest(String nickname, String cards) throws RemoteException;

    /**
     * Skips the current player's turn.
     *
     * @param nickname the player's nickname
     * @throws RemoteException if the remote call fails
     */
    void skipTurn(String nickname) throws RemoteException;

    /**
     * Notifies the server that the client is disconnecting.
     *
     * @param nickname the player's nickname
     * @throws RemoteException if the remote call fails
     */
    void disconnect(String nickname) throws RemoteException;

    void pong(String nickname) throws RemoteException;
}
