package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote server-side interface exposed via RMI.
 * Clients obtain a stub from the registry and invoke these methods to send actions.
 */
public interface RMIGameServer extends Remote {
    /**
     * Registers the client and its callback for server-to-client notifications.
     * This is called by the client right after obtaining the stub.
     *
     * @param nickname   the player's nickname
     * @param numPlayers desired total number of players (only used by the first player)
     * @param callback   the client callback for server notifications
     * @throws Exception if registration fails
     */
    void register(String nickname, int numPlayers, RMIClientCallback callback) throws Exception;

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
}
