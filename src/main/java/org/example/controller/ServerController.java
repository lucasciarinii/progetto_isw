package org.example.controller;

import org.example.model.enums.GamePhase;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerController {
    private Match match;
    private Map<ClientConnection, String> clientNicknames = new HashMap<>();

    public ServerController(Match match) {
        this.match = match;
    }

    public void placeTotemOnOfferTile(Socket sender, int tilePosition) {
        String nick = clientNicknames.get(sender);
        Player player = match.getPlayers().stream().filter(p -> p.getNickname().equals(nick)).findFirst().orElse(null);

        if (!isCorrectPlayer(nick) || !isCorrectPhase(GamePhase.PLACE_TOTEMS)) {
            //send(sender, new ErrorMessage("Mossa non valida"));
            return;
        }
        // update model
        match.placeTotemOnOfferTile(player, tilePosition);
        match.getGameState().advanceToNextPlayer();
        //handlePhaseTransition();
    }

    public void offerTileAction(Socket sender, String cards) {
        String nick = clientNicknames.get(sender);
        Player player = match.getPlayers().stream().filter(p -> p.getNickname().equals(nick)).findFirst().orElse(null);

        if (!isCorrectPlayer(nick) || !isCorrectPhase(GamePhase.PLAYER_TURN)) {
            //send(sender, new ErrorMessage("Mossa non valida"));
            return;
        }
        // update model
        match.offerTileAction(player, cards);
        match.getGameState().advanceToNextPlayer();
        //handlePhaseTransition();
    }

    // Private Utility Methods
    private boolean isCorrectPlayer(String nick) {
        return match.getGameState().getCurrentPlayer().getNickname().equals(nick);
    }

    private boolean isCorrectPhase(GamePhase expected) {
        return match.getGameState().getCurrentPhase() == expected;
    }

//    private void send(Socket socket, Message msg) {
//        try { outputStreams.get(socket).writeObject(msg); }
//        catch (IOException e) { unregisterClient(socket); }
//    }
//
//    private void notifyAll(Message msg) {
//        outputStreams.keySet().forEach(s -> send(s, msg));
//    }
//
//    private GameStateUpdateMessage buildGameStateUpdate() {
//        // costruisce snapshot dello stato attuale da mandare ai client
//    }


}
