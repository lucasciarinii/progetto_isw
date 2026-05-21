package org.example.network.rmi;

import org.example.client.rmi.RMIClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

//? Remote interface SERVER-side -> The "contract" of what server can do for clients.
//? Client gets a stub of this interface and from Registry and uses it to send commands to the server.
public interface RMIGameServer extends Remote {

    /* Create or join a game with the client callback used for server updates. */
    String createGame(String nickname, int numPlayers, RMIClientCallback callback) throws Exception;

    void joinGame(String nickname, String gameID, RMIClientCallback callback) throws Exception;

    // Player places his totem on an offer tile. nickname -> who is placing the totem, tilePosition -> which offer tile (1-based)
    void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException;

    // Player resolves action from the offerTile he is currently on. nickname -> who is resolving the offer tile, cards -> string with IDs selected
    void offerTileAction(String nickname, String cards) throws RemoteException;

    void roundFlowCardRequest(String nickname, String cards) throws RemoteException;

    void skipTurn(String nickname) throws RemoteException;
}
