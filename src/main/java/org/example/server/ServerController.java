package org.example.server;

import org.example.network.RankingUpdateMessage;
import org.example.server.database.GameDAO;
import org.example.server.database.RankingEntry;
import org.example.server.model.board.OfferTile;
import org.example.network.GameStateUpdateMessage;
import org.example.network.Snapshots.OfferTileSnapshot;
import org.example.network.Snapshots.PlayerSnapshot;
import org.example.network.Snapshots.TurnSlotSnapshot;
import org.example.server.model.enums.OfferEffect;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.rmi.RMIClientConnection;
import org.example.server.model.board.Board;
import org.example.server.model.board.PlayerSlot;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.match.GameState;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
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

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLACE_TOTEMS)) {
            sendError(sender, "Invalid move: it's not yourn turn or invalid phase.");
            return;
        }

        // update model
        try {
            Player player = getPlayerByNickname(nickname);
            GamePhase phaseBefore = match.getGameState().getCurrentPhase();
            match.placeTotemOnOfferTile(player, tilePosition);
            match.getGameState().advanceToNextPlayer();
            handlePhaseTransition(phaseBefore);
        } catch (Exception e) {
            sendError(sender, "Invalid move: " + e.getMessage());
        }

    }

    public void offerTileAction(String nickname, String cards) {
        ClientConnection sender = findConnection(nickname);
        if (sender == null) {
            System.err.println("[SERVER] Connection not found for: " + nickname);
            return;
        }

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLAYER_TURN)) {
            sendError(sender, "Invalid move: it's not yourn turn or invalid phase.");
            return;
        }

        try {
            Player player = getPlayerByNickname(nickname);
            GamePhase phaseBefore = match.getGameState().getCurrentPhase();
            match.offerTileAction(player, cards);
            match.getGameState().advanceToNextPlayer();
            handlePhaseTransition(phaseBefore);
        } catch (NoDrawableCardException e) {
            // We must still move the player to the TurnOrderTile
            OfferTile selectedTile = match.getBoard().getOfferTrack().stream()
                    .filter(tile -> tile.getPlayer() != null )
                    .filter(tile -> tile.getPlayer().getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow( () -> new IllegalStateException( "player not found on offerTrack") );
            selectedTile.removePlayer();

            // Warn about the NoDrawableCard and skip his turn
            sendError(sender, e.getMessage());
            GamePhase phaseBefore = match.getGameState().getCurrentPhase();
            match.getGameState().advanceToNextPlayer();
            handlePhaseTransition(phaseBefore);
        }
        catch (InvalidCardException e) {
            sendError(sender, "Invalid move: " + e.getMessage());
        }
        catch (Exception e) {
            sendError(sender, "Generic Exception: " + e.getMessage());
        }

    }

    public void skipTurn(String nickname) {
        ClientConnection sender = findConnection(nickname);
        if (sender == null) {
            System.err.println("[SERVER] SkipTurn failed: connection not found for: " + nickname);
            return;
        }

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLAYER_TURN)) {
            sendError(sender, "SkipTurn failed: invalid move: it's not yourn turn or invalid phase.");
            return;
        }

        GamePhase phaseBefore = match.getGameState().getCurrentPhase();
        match.getGameState().advanceToNextPlayer();
        handlePhaseTransition(phaseBefore);
    }

    //! UTILITY METHODS ---------------------------------------------------------------------------
    private boolean isWrongPlayer(String nick) {
        return !match.getGameState().getCurrentPlayer().getNickname().equals(nick);
    }

    private boolean isWrongPhase(GamePhase expected) {
        return !(match.getGameState().getCurrentPhase() == expected);
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
                System.out.print("[SERVER] Failed to send update to " + clientNicknames.get(client) + ", unregistering client. Reason: " + e.getMessage());
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
                        p.getDiscountOnBuilding(),
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

    //! HANDLE PHASE TRANSITIONS
    private void handlePhaseTransition(GamePhase phaseBefore) {
        GamePhase phaseAfter = match.getGameState().getCurrentPhase();

        if (phaseBefore == phaseAfter) {
            // Same phase, just the current player did the action
            notifyAll(buildSnapshot());
            return;
        }

        // Phase changed: all players have completed the previous phase
        handleNewPhase(phaseAfter);
    }

    private void handleNewPhase(GamePhase phase) {
        switch (phase) {

            case PLACE_TOTEMS -> {
                // PLACE_TOTEMS: new round
                // reads the TurnOrderTile and update the Turn Order for PLACE_TOTEMS
                List<Player> newTurnOrder = match.getBoard().getTurnOrderTile().getSlots().stream()
                        .map(PlayerSlot::getPlayer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                match.getGameState().updateTurnOrder(newTurnOrder);
                notifyAll(buildSnapshot());
                // ← wait for clients inputs (placeTotemOnOfferTile)
            }

            case PLAYER_TURN -> {
                // PLACE_TOTEMS → PLAYER_TURN
                // Turns order becomes the order of players on the offer track (left to right)
                List<Player> offerOrder = match.getBoard().getOfferTrack().stream()
                        .filter(t -> t.getPlayer() != null)
                        .map(OfferTile::getPlayer)
                        .collect(Collectors.toList());
                match.getGameState().updateTurnOrder(offerOrder);
                notifyAll(buildSnapshot());
                // ← wait clients inputs (offerTileAction)
            }

            case EVENTS_RESOLVE -> {
                // PLAYER_TURN → EVENTS_RESOLVE: automatic, no inputs
                // Resolve bottom row events
                match.resolveBottomEvents();

                // Immediate advances to END_ROUND
                match.getGameState().advancePhase();
                handleNewPhase(match.getGameState().getCurrentPhase());
            }

            case END_ROUND -> {
                // EVENTS_RESOLVE → END_ROUND: automatic
                if(match.getGameState().getCurrentRound() == 10) {
                    // If it's the end of round 10, we go directly to END_GAME
                    match.getGameState().advancePhase();
                    handleNewPhase(match.getGameState().getCurrentPhase());
                    return;
                }
                match.endRoundOperations();

                // END_ROUND.next(state) handle internally:
                //   - round < 10  → advanceRound() + PLACE_TOTEMS
                //   - round == 10 → END_GAME
                match.getGameState().advancePhase();
                handleNewPhase(match.getGameState().getCurrentPhase());
            }

            case END_GAME -> {
                // END_ROUND → END_GAME: round 10 completed
                match.endOfGame();

                // Avanza a GAME_OVER
                match.getGameState().advancePhase();
                handleNewPhase(GamePhase.GAME_OVER);
            }

            case GAME_OVER -> {
                // FINAL STATE: notifies all clients with the final snapshot (winners included)
                notifyAll(buildSnapshot());

                // DB: save game results in the DB
                // 1) Build results and placements
                Map<String, Integer> results    = new HashMap<>();
                Map<String, Integer> placements = new HashMap<>();

                List<Player> sorted = match.getPlayers().stream()
                        .sorted(Comparator.comparingInt(Player::getPoints).reversed())
                        .toList();

                for (int i = 0; i < sorted.size(); i++) {
                    Player p = sorted.get(i);
                    results.put(p.getNickname(), p.getPoints());
                    placements.put(p.getNickname(), i + 1);
                }

                // 2) Store on DB
                GameDAO dao = new GameDAO();
                try {
                    dao.saveGame(match.getPlayers().size(), results, placements);
                } catch (SQLException e) {
                    System.err.println("[DB ERROR] Failed to save game: " + e.getMessage());
                }

                // 3) Execute query and get results
                try {
                    List<RankingEntry> ranking = dao.getRanking(match.getPlayers().size());
                    for (Player p : match.getPlayers()) {
                        int rankPosition = dao.getPlayerGlobalRank(p.getNickname(), match.getPlayers().size());
                        RankingUpdateMessage msg = new RankingUpdateMessage(ranking, rankPosition);

                        // Cerca la ClientConnection corrispondente al nickname
                        clientNicknames.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(p.getNickname()))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .ifPresent(conn -> {
                                    try {
                                        conn.sendRankingUpdate(msg);
                                    } catch (RemoteException e) {
                                        System.err.println("[ERROR] Failed to send ranking to " + p.getNickname());
                                    }
                                    catch (Exception e) {
                                        System.err.println("[ERROR] Failed to send ranking to " + p.getNickname() + ": " + e.getMessage());
                                    }
                                });
                    }
                } catch (SQLException e) {
                    System.err.println("[DB ERROR] Failed to retrieve ranking: " + e.getMessage());
                }

                // TODO: GESTIRE L'EVENTUALE CHIUSURA DELLE CONNESSIONI
            }

             default -> throw new IllegalStateException("Unexpected phase: " + phase);
        }
    }


}
