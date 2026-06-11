package org.example.server;

import org.example.network.ServerNotifier;
import org.example.network.messages.GameStateUpdateMessage;
import org.example.network.messages.RankingUpdateMessage;
import org.example.network.snapshots.OfferTileSnapshot;
import org.example.network.snapshots.PlayerSnapshot;
import org.example.network.snapshots.TurnSlotSnapshot;
import org.example.server.database.GameDAO;
import org.example.server.database.RankingEntry;
import org.example.server.model.board.Board;
import org.example.server.model.board.OfferTile;
import org.example.server.model.board.PlayerSlot;
import org.example.server.model.cards.buildingCards.RoundFlowBC;
import org.example.server.model.enums.GamePhase;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;
import org.example.server.model.match.GameState;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Server-side controller that validates player actions, updates the match state,
 * and broadcasts snapshots to connected clients.
 */
public class ServerController implements Runnable {
    private final Match match;
    private final ServerNotifier notifier;
    private GameOverListener onGameOver;
    private boolean waitingForRoundFlow = false;
    private String roundFlowPlayerNick = null;


    //! CONSTRUCTOR ---------------------------------------------------------------------------
    public ServerController(Match match, ServerNotifier notifier) {
        this.match = match;
        this.notifier = notifier;
    }

    /**
     * Starts the controller execution by sending the initial game state to all players.
     */
    @Override
    public void run() {
        sendInitialState();
    }

    /**
     * Registers a listener to be called when the game ends.
     *
     * @param listener callback invoked after the game-over phase is processed
     */
    public void setGameOverListener(GameOverListener listener) {
        this.onGameOver = listener;
    }

    /**
     * Returns the list of players participating in the current match.
     *
     * @return the players of the current match
     */
    public List<Player> getPlayers() {
        return match.getPlayers();
    }

    /**
     * Finalizes the session and notifies the game-over listener, if any.
     */
    private void shutdown() {
        if (onGameOver != null) {
            onGameOver.onGameOver(this);
        }
    }

    //! GAME ACTIONS ---------------------------------------------------------------------------

    /**
     * Places a player's totem on the selected offer tile.
     *
     * @param nickname     the player nickname
     * @param tilePosition the offer tile index
     */
    public void placeTotemOnOfferTile(String nickname, int tilePosition) {
        if (!isKnownPlayer(nickname)) {
            ServerLogger.server("Player not found for: " + nickname);
            return;
        }

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLACE_TOTEMS)) {
            sendError(nickname, "Invalid move: it's not yourn turn or invalid phase.");
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
            sendError(nickname, "Invalid move: " + e.getMessage());
        }

    }

    /**
     * Executes the action for the offer tile selected by the current player.
     *
     * @param nickname the player nickname
     * @param cards    the serialized card selection
     */
    public void offerTileAction(String nickname, String cards) {
        if (!isKnownPlayer(nickname)) {
            ServerLogger.server("Player not found for: " + nickname);
            return;
        }

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLAYER_TURN)) {
            sendError(nickname, "Invalid move: it's not yourn turn or invalid phase.");
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
            sendError(nickname, e.getMessage());
            GamePhase phaseBefore = match.getGameState().getCurrentPhase();
            match.getGameState().advanceToNextPlayer();
            handlePhaseTransition(phaseBefore);
        }
        catch (InvalidCardException e) {
            sendError(nickname, "Invalid move: " + e.getMessage());
        }
        catch (Exception e) {
            sendError(nickname, "Generic Exception: " + e.getMessage());
        }

    }

    /**
     * Handles the RoundFlow building request when a player must choose the extra top card.
     *
     * @param nickname the player nickname
     * @param cards    the card ID
     */
    public void roundFlowCardRequest(String nickname, String cards) {
        if (!waitingForRoundFlow) { // waitingForRoundFlow is set to true only if we are in END_ROUND and a player has RoundFlowBC, so we expect this request only in that case
            try {
                notifier.sendError(nickname, "No RoundFlow action expected now.",
                        match.getGameState().getCurrentPhase());
            } catch (Exception e) {
                ServerLogger.error("Failed to send error: " + e.getMessage());
            }
            return;
        }

        if (!nickname.equals(roundFlowPlayerNick)) {
            try {
                notifier.sendError(nickname, "It's not your RoundFlow turn.",
                        match.getGameState().getCurrentPhase());
            } catch (Exception e) {
                ServerLogger.error("Failed to send error: " + e.getMessage());
            }
            return;
        }

        Player player = getPlayerByNickname(nickname);
        try {
            match.roundFlowCardRequest(player, cards);
        }
        catch (InvalidCardException e) {
            sendError(nickname, "Invalid move: " + e.getMessage());
            try {
                notifier.sendRoundFlowCardRequest(nickname);
            } catch (Exception ex) {
                ServerLogger.error("Failed to resend RoundFlow request: " + ex.getMessage());
            }
            return;
        }
        catch (Exception e) {
            sendError(nickname, "Generic Exception: " + e.getMessage());
            try {
                notifier.sendRoundFlowCardRequest(nickname);
            } catch (Exception ex) {
                ServerLogger.error("Failed to resend RoundFlow request: " + ex.getMessage());
            }
            return;
        }

        waitingForRoundFlow = false;
        roundFlowPlayerNick = null;

        // Notify all players with the updated state, then continue.
        notifyAll(buildSnapshot());
        proceedEndRound();

    }

    /**
     * Skips the current player's turn and advances the phase if needed. (called when there are no drawable cards for the player on his turn)
     *
     * @param nickname the player nickname
     */
    public void skipTurn(String nickname) {
        if (!isKnownPlayer(nickname)) {
            ServerLogger.server("SkipTurn failed: player not found for: " + nickname);
            return;
        }

        if (isWrongPlayer(nickname) || isWrongPhase(GamePhase.PLAYER_TURN)) {
            sendError(nickname, "SkipTurn failed: invalid move: it's not yourn turn or invalid phase.");
            return;
        }

        // Remove the player's totem from the offer track before advancing
        match.getBoard().getOfferTrack().stream()
                .filter(tile -> tile.getPlayer() != null)
                .filter(tile -> tile.getPlayer().getNickname().equals(nickname))
                .findFirst()
                .ifPresent(OfferTile::removePlayer);

        GamePhase phaseBefore = match.getGameState().getCurrentPhase();
        match.getGameState().advanceToNextPlayer();
        handlePhaseTransition(phaseBefore);
    }

    //! UTILITY METHODS ---------------------------------------------------------------------------
    /**
     * Checks whether the given nickname does not match the current player in turn.
     *
     * @param nick the nickname to verify
     * @return {@code true} if the nickname does not belong to the current player
     */
    private boolean isWrongPlayer(String nick) {
        return !match.getGameState().getCurrentPlayer().getNickname().equals(nick);
    }

    /**
     * Checks whether the current game phase differs from the expected one.
     *
     * @param expected the expected game phase
     * @return {@code true} if the current phase is different from the expected one
     */
    private boolean isWrongPhase(GamePhase expected) {
        return !(match.getGameState().getCurrentPhase() == expected);
    }

    /**
     * Checks whether the given nickname belongs to a player in the current match.
     *
     * @param nickname the nickname to verify
     * @return {@code true} if the player exists in the match
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isKnownPlayer(String nickname) {
        return match.getPlayers().stream().anyMatch(p -> p.getNickname().equals(nickname));
    }

    /**
     * Returns the player associated with the given nickname.
     *
     * @param nick the player's nickname
     * @return the matching player
     */
    private Player getPlayerByNickname(String nick) {
        return match.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nick))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + nick));
    }

    //! NOTIFICATION METHODS ---------------------------------------------------------------------------
    /**
     * Sends a game-state update to all connected players.
     *
     * @param update the snapshot DTO to broadcast
     */
    private void notifyAll(GameStateUpdateMessage update) {
        for (Player player : match.getPlayers()) {
            try {
                notifier.sendGameStateUpdate(player.getNickname(), update);
            } catch (Exception e) {
                ServerLogger.server("Failed to send update to " + player.getNickname() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Sends an error message to the specified player.
     *
     * @param nickname the target player nickname
     * @param message  the error description
     */
    private void sendError(String nickname, String message) {
        try {
            String safeMessage = (message == null || message.isBlank()) ? "Unknown error" : message;
            notifier.sendError(nickname, safeMessage, match.getGameState().getCurrentPhase());
        } catch (Exception e) {
            ServerLogger.server("Failed to send error to " + nickname + ": " + e.getMessage());
        }
    }

    /**
     * Sends the initial snapshot to all clients when the game starts.
     */
    public void sendInitialState() {
        notifyAll(buildSnapshot());
    }

    /**
     * Builds a DTO snapshot of the current match state.
     *
     * @return the game-state update message
     */
    private GameStateUpdateMessage buildSnapshot() {
        // builds DTO from current Match state
        GameState gs = match.getGameState();
        Board board = match.getBoard();


        // 1. offerTrack: snapshot of OfferTrack (list of OfferTileSnapshot)
        List<OfferTileSnapshot> offerTrack = board.getOfferTrack().stream()
                .map(tile -> new OfferTileSnapshot(
                        tile.getOfferEffect(),
                        tile.getPlayer() != null ? tile.getPlayer().getNickname() : null
                ))
                .collect(Collectors.toList());

        // 2. turnOrderSlots: snapshot of TurnOrderTile
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

        // 3. players: snapshot of each player
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

        // 4. winners: winner/winners nicknames (empty if not over)
        List<String> winners = gs.getWinners().stream()
                .map(Player::getNickname)
                .collect(Collectors.toList());

        return new GameStateUpdateMessage(
                gs.getCurrentRound(),
                gs.getCurrentEra(),
                gs.getCurrentPhase(),
                gs.getCurrentPlayer().getNickname(),
                new ArrayList<>(board.getTopRow()),
                new ArrayList<>(board.getBottomRow()),
                offerTrack,
                turnOrderSlots,
                players,
                winners
        );
    }

    //! HANDLE PHASE TRANSITIONS

    /**
     * Handles phase transitions after a player action.
     *
     * @param phaseBefore the phase before the action
     */
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

    /**
     * Executes the logic associated with entering a new phase.
     *
     * @param phase the new game phase
     */
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
                        .map(OfferTile::getPlayer)
                        .filter(Objects::nonNull)
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
                Player roundFlowPlayer = match.getPlayers().stream()
                        .filter(p -> p.getOwnedBuildings().stream()
                                .anyMatch(c -> c instanceof RoundFlowBC))
                        .findFirst()
                        .orElse(null);

                if (roundFlowPlayer != null) {
                    waitingForRoundFlow = true;
                    roundFlowPlayerNick = roundFlowPlayer.getNickname();
                    // Ensure clients see who must answer the RoundFlow request.
                    match.getGameState().setCurrentPlayer(roundFlowPlayer);
                    notifyAll(buildSnapshot());
                    try {
                        notifier.sendRoundFlowCardRequest(roundFlowPlayerNick);
                    } catch (Exception e) {
                        ServerLogger.error("Failed to send RoundFlow request: " + e.getMessage());
                    }
                    // stop — resume in roundFlowCardRequest()
                    return;
                }

                proceedEndRound();
            }

            case END_GAME -> {
                // END_ROUND → END_GAME: round 10 completed
                match.endOfGame();

                // Move to GAME_OVER.
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
                    ServerLogger.db_error("Failed to save game: " + e.getMessage());
                }

                // 3) Execute query and get results
                try {
                    List<RankingEntry> ranking = dao.getRanking(match.getPlayers().size());
                    for (Player p : match.getPlayers()) {
                        int rankPosition = dao.getPlayerGlobalRank(p.getNickname(), match.getPlayers().size());
                        RankingUpdateMessage msg = new RankingUpdateMessage(ranking, rankPosition);
                        try {
                            notifier.sendRankingUpdate(p.getNickname(), msg);
                        } catch (Exception e) {
                            ServerLogger.db_error("Failed to send ranking to " + p.getNickname() + ": " + e.getMessage());
                        }
                    }
                } catch (SQLException e) {
                    ServerLogger.db_error("Failed to retrieve ranking: " + e.getMessage());
                }

                // 4) Now we can send shutdown to all clients (of this match)
                for (Player player : match.getPlayers()) {
                    try {
                        notifier.sendShutdown(player.getNickname());
                    } catch (Exception e) {
                        ServerLogger.error("Failed to send shutdown to " + player.getNickname() + ": " + e.getMessage());
                    }
                }

                // Close connection
                shutdown();
            }

             default -> throw new IllegalStateException("Unexpected phase: " + phase);
        }
    }

    /**
     * Finalizes the end-of-round flow, including end-game transition.
     */
    private void proceedEndRound() {
        if (match.getGameState().getCurrentRound() == 10) {
            match.getGameState().advancePhase();
            handleNewPhase(match.getGameState().getCurrentPhase());
            return;
        }
        match.endRoundOperations();
        match.getGameState().advancePhase();
        handleNewPhase(match.getGameState().getCurrentPhase());
    }



}
