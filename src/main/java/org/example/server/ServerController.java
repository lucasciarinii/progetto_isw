package org.example.server;

import org.example.network.GameStateUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.network.Snapshots.TurnSlotSnapshot;
import org.example.server.rmi.RMIClientConnection;
import org.example.model.board.Board;
import org.example.model.board.PlayerSlot;
import org.example.model.enums.GamePhase;
import org.example.model.match.GameState;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerController {
    private final Match match;
    // Maps each connected client to the corresponding player nickname (set at connection time)
    private Map<ClientConnection, String> clientNicknames = new HashMap<>();

    //! CONSTRUCTOR ---------------------------------------------------------------------------
    public ServerController(Match match) {
        this.match = match;
    }

    //! CLIENT REGISTRATION ---------------------------------------------------------------------------
    public void registerClient(ClientConnection client, String nickname) {
        clientNicknames.put(client, nickname);
    }

    public void unregisterClient(ClientConnection client) {
        clientNicknames.remove(client);
    }

    //! GAME ACTIONS ---------------------------------------------------------------------------
    public void placeTotemOnOfferTile(String nickname, int tilePosition) {
        ClientConnection sender = findConnection(nickname);
        if (sender == null) {
            System.err.println("[SERVER] Connection not found for: " + nickname);
            return;
        }

        if (!isCorrectPlayer(nickname) || !isCorrectPhase(GamePhase.PLACE_TOTEMS)) {
            sendError(sender, "Invalid move: it's not yourn turn or invalid phase.");
            return;
        }

        // update model
        try {
            Player player = getPlayerByNickname(nickname);
            match.placeTotemOnOfferTile(player, tilePosition);
            match.getGameState().advanceToNextPlayer();
            notifyAll(buildSnapshot());
        } catch (Exception e) {
            sendError(sender, "Invalid move: " + e.getMessage());
        }

        // TODO:  GESTIRE LA TRANSIZIOEN DI FASE?
    }

    public void offerTileAction(String nickname, String cards) {
        ClientConnection sender = findConnection(nickname);
        if (sender == null) {
            System.err.println("[SERVER] Connessione non trovata per: " + nickname);
            return;
        }

        if (!isCorrectPlayer(nickname) || !isCorrectPhase(GamePhase.PLAYER_TURN)) {
            sendError(sender, "Mossa non valida: non è il tuo turno o fase errata.");
            return;
        }

        try {
            Player player = getPlayerByNickname(nickname);
            match.offerTileAction(player, cards);
            match.getGameState().advanceToNextPlayer();
            notifyAll(buildSnapshot());
        } catch (Exception e) {
            sendError(sender, "Mossa non valida: " + e.getMessage());
        }

        // TODO: GESTIRE LA TRANSIZIOEN DI FASE?
    }

    //! UTILITY METHODS ---------------------------------------------------------------------------
    private boolean isCorrectPlayer(String nick) {
        return match.getGameState().getCurrentPlayer().getNickname().equals(nick);
    }

    private boolean isCorrectPhase(GamePhase expected) {
        return match.getGameState().getCurrentPhase() == expected;
    }

    private Player getPlayerByNickname(String nick) {
        return match.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nick))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + nick));
    }

    public RMIClientConnection getConnectionByNickname(String nickname) {
        return (RMIClientConnection) clientNicknames.entrySet().stream()
                .filter(e -> e.getValue().equals(nickname))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + nickname));
    }

    // Internal lookup to get the ClientConnection associated with a nickname
    private ClientConnection findConnection(String nickname) {
        return clientNicknames.entrySet().stream()
                .filter(e -> e.getValue().equals(nickname))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    //! NOTIFICATION METHODS ---------------------------------------------------------------------------
    // Notifies all clients with the DTO (GameStateUpdateMessage) built from the current Match state with buildSnapshot() method.
    private void notifyAll(GameStateUpdateMessage update) {
        clientNicknames.keySet().forEach(client -> {
            try {
                client.sendUpdate(update);
            } catch (Exception e) {
                // Client disconnected, remove it
                unregisterClient(client);
            }
        });
    }

    // Notifies the sender client with an error message (e.g., invalid move, wrong turn, etc.). If sending fails (e.g., client disconnected), we unregister the client.
    private void sendError(ClientConnection client, String message) {
        try {
            client.sendError(message);
        } catch (Exception e) {
            unregisterClient(client);
        }
    }

    // Sends the initial snapshot to all clients when the game exactly starts. Called by LobbyController after registering all clients.
    public void sendInitialState() {
        notifyAll(buildSnapshot());
    }

    //! BUILD SNAPSHOT METHOD ---------------------------------------------------------------------------
    // Method used to read 'Match' and builds the corresponding DTO (GameStateUpdateMessage) to send to the clients.
    private GameStateUpdateMessage buildSnapshot() {
        // builds DTO from current Match state
        GameState gs = match.getGameState();
        Board board = match.getBoard();

        // 1. turnOrder: nicknames list in the correct turn order
        List<String> turnOrder = gs.getTurnOrder().stream()
                .map(Player::getNickname)
                .collect(Collectors.toList());

        // 2. offerTrack: snapshot of OfferTrack (list of OfferTileSnapshot)
        List<OfferTileSnapshot> offerTrack = board.getOfferTrack().stream()
                .map(tile -> new OfferTileSnapshot(
                        tile.getOfferEffect(),
                        tile.getPlayer() != null ? tile.getPlayer().getNickname() : null
                ))
                .collect(Collectors.toList());

        // 3. turnOrderSlots: snapshot of TurnOrderTile
        List<PlayerSlot> slots = board.getTurnOrderTile().getSlots();
        List<TurnSlotSnapshot> turnOrderSlots = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            PlayerSlot slot = slots.get(i);
            turnOrderSlots.add(new TurnSlotSnapshot(
                    i,
                    slot.getFood(),
                    slot.getPoints(),
                    slot.getPlayer() != null ? slot.getPlayer().getNickname() : null
            ));
        }

        // 4. players: snapshot of each player
        List<PlayerSnapshot> players = match.getPlayers().stream()
                .map(p -> new PlayerSnapshot(
                        p.getNickname(),
                        p.getFood(),
                        p.getPoints(),
                        new ArrayList<>(p.getHunters()),
                        new ArrayList<>(p.getGatherers()),
                        new ArrayList<>(p.getBuilders()),
                        new ArrayList<>(p.getShamans()),
                        new ArrayList<>(p.getArtists()),
                        new ArrayList<>(p.getInventors()),
                        new ArrayList<>(p.getOwnedBuildings())
                ))
                .collect(Collectors.toList());

        // 5. winners: winner/winners nicknames (empty if not over)
        List<String> winners = gs.getWinners().stream()
                .map(Player::getNickname)
                .collect(Collectors.toList());

        return new GameStateUpdateMessage(
                gs.getCurrentRound(),
                gs.getCurrentEra(),
                gs.getCurrentPhase(),
                gs.getCurrentPlayer().getNickname(),
                turnOrder,
                new ArrayList<>(board.getTopRow()),
                new ArrayList<>(board.getBottomRow()),
                offerTrack,
                turnOrderSlots,
                players,
                winners
        );
    }



}
