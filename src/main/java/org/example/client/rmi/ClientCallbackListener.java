package org.example.client.rmi;

import org.example.network.GameStateUpdateMessage;
import org.example.network.LobbyUpdateMessage;

//? This interfaces decouples the ClientCallbackImpl from the ClientController. Whoever wants to receive updates or errors from server implements this interface.
public interface ClientCallbackListener {

    void onUpdate(GameStateUpdateMessage update);

    void onError(String errorMessage);

    void onLobbyUpdate(LobbyUpdateMessage update);

}
