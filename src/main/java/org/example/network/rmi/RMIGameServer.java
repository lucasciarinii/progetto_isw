package org.example.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

//? Remote interface SERVER-side -> The "contract" of what server can do for clients.
//? Client gets a stub of this interface and from Registry and uses it to send commands to the server.
public interface RMIGameServer extends Remote {

    /* Register the client: associate client's nickname with the callback object that the server will use to send updates to the client.
       It is called by the client right after connecting to the server and getting the stub, to complete the registration process.
        @param numPlayers: used just by first player */
    void register(String nickname, int numPlayers, RMIClientCallback callback) throws Exception;

    // Player places his totem on an offer tile. nickname -> who is placing the totem, tilePosition -> which offer tile (1-based)
    void placeTotemOnOfferTile(String nickname, int tilePosition) throws RemoteException;

    // Player resolves action from the offerTile he is currently on. nickname -> who is resolving the offer tile, cards -> string with IDs selected
    void offerTileAction(String nickname, String cards) throws RemoteException;

    void skipTurn(String nickname) throws RemoteException;
}
