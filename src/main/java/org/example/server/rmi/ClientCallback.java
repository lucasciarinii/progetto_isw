package org.example.server.rmi;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

//? Remote interface CLIENT-side -> The "contract" of what the client must implement for the server to be able to send updates and errors.
//? Client implements this interface and exposes a remote object to the server, so that the server can use to send updated or errors
public interface ClientCallback extends Remote {

    // Server sends snapshot updated of the game state. The client will receive it and use this to update its view.
    void receiveUpdate(GameStateUpdateMessage update) throws RemoteException;

    // Server sends an error message to the client, for example if the client made an invalid move. The client will receive it and use this.
    void receiveError(String errorMessage) throws RemoteException;

    // Server sends snapshot updated of the lobby state. The client will receive it.
    void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException;
}
