package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.client.rmi.RMIClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote server-side interface exposed via RMI.
 * Clients obtain a stub from the registry and invoke these methods to send actions.
 */
public interface RMIGameServer extends Remote {

    /**
     * Creates a new lobby, registers the first player, and reserves its nickname.
     * The first player also defines the number of players expected for the match.
     *
     * @param nickname the chosen nickname of the player creating the lobby
     * @param numPlayers the total number of players expected in the lobby
     * @param callback the remote callback used by the server to notify the client
     *
     * @return the generated game ID for the new lobby
     * @throws Exception if lobby creation, nickname reservation, or player registration fails
     */
    String createLobby(String nickname, int numPlayers, RMIClientCallback callback) throws Exception;

    /**
     * Joins an existing lobby using the provided game ID and registers the client callback.
     *
     * @param nickname the chosen nickname of the joining player
     * @param gameID the ID of the lobby to join
     * @param callback the remote callback used by the server to notify the client
     * @throws Exception if the lobby does not exist, is already started, the nickname is already used, or player registration fails
     */
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

    /**
     * Acknowledges a ping from the server heartbeat, confirming the client is alive.
     * Invoked by the client after receiving {@code receivePing()} on its callback.
     *
     * @param nickname the player's nickname
     * @throws RemoteException if the remote call fails
     */
    void pong(String nickname) throws RemoteException;
}
