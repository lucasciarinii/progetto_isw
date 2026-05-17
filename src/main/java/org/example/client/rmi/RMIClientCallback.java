package org.example.client.rmi;

import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.LobbyUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.server.model.enums.GamePhase;

import java.rmi.Remote;
import java.rmi.RemoteException;

//? Remote interface CLIENT-side -> The "contract" of what the client must implement for the server to be able to send updates and errors.
//? Client implements this interface and exposes a remote object to the server, so that the server can use to send updated or errors
public interface RMIClientCallback extends Remote {

    // Server sends snapshot updated of the game state. The client will receive it and use this to update its view.
    void receiveUpdate(GameStateUpdateMessage update) throws RemoteException;

    // Server sends an error message to the client, for example if the client made an invalid move. The client will receive it and use this.
    void receiveError(String errorMessage, GamePhase phase) throws RemoteException;

    // Server sends snapshot updated of the lobby state. The client will receive it.
    void receiveLobbyUpdate(LobbyUpdateMessage update) throws RemoteException;

    // Server sends results of queries to the client.
    void receiveRankingUpdate(RankingUpdateMessage rankingUpdate) throws RemoteException;

    void receiveRoundFlowCardRequest();

    // Server sends a shutdown message to the clients in order to handle first sending ranking update
    void receiveShutdown() throws RemoteException;

}
