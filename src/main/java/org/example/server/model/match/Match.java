package org.example.server.model.match;

import org.example.server.model.board.Board;
import org.example.server.model.board.OfferTile;
import org.example.server.model.board.PlayerSlot;
import org.example.server.model.board.turnOrderTileActions.OfferActionRegistry;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.cards.buildingCards.RoundFlowTotemBC;
import org.example.server.model.cards.characters.Builder;
import org.example.server.model.cards.characters.Inventor;
import org.example.server.model.cards.eventCards.EventCard;
import org.example.server.model.cards.eventCards.Sustenance;
import org.example.server.model.enums.OfferEffect;
import org.example.server.model.exceptions.InvalidCardException;
import org.example.server.model.exceptions.NoDrawableCardException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single match and acts as the main model entry point for the controller.
 * It owns the players, board, and game state, and applies the rules for each phase.
 */
public class Match {

    private final List<Player> players;
    private Board board;
    private GameState gameState;
    private final OfferActionRegistry offerActionRegistry = new OfferActionRegistry(); // Registry of actions for offer effects, used to apply the correct move.

    public Match(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");
        this.players = new ArrayList<>(players);

        init(); // initialize board and gameState
    }
    public List<Player> getPlayers(){return Collections.unmodifiableList(players);}

    public Board getBoard(){return board;}

    public GameState getGameState(){return gameState;}

    /**
     * Initialize the game:
     * - Randomize players order
     * - Initialize the board (with decks, tiles, etc.)
     * - Distribute starting food based on player order (rules)
     * - Initialize GameState
     */
    private void init() { // initialize board and gameState following the setup step
        // Steps 9-10: randomize player order.
        Collections.shuffle(players);

        // Steps 1-8: delegate setup to Board (it relies on the randomized player list).
        board = new Board(players);

        // Step 11: distribute starting food based on player order.
        for (int i = 0; i < players.size(); i++) {
            int food = switch (i) {
                case 0 -> 2;
                case 1, 2 -> 3;
                case 3, 4 -> 4;
                default -> throw new IllegalArgumentException("Invalid list of players");
            };
            players.get(i).addFood(food);
        }

        // Initialize GameState (order is already randomized).
        gameState = new GameState(players);

    }

    //! METHODS TO MANAGE THE MATCH

    /**
     * Executes setup steps when a new era begins.
     */
    public void newEraOperations() {
        // 1. Discard any building cards present in the bottom row.
        board.getBottomRow().removeIf(Card::isBuilding);

        // 2. Move building cards from the top row to the bottom row, to the right of Tribe cards.
        List<Card> buildingCardsInTopRow = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card.isBuilding()) {
                buildingCardsInTopRow.add(card);
                return true; // remove from top row
            }
            return false; // keep in top row
        });
        board.getBottomRow().addAll(buildingCardsInTopRow);

        // 3. Place the new-era building card in the top row, to the right of Tribe cards.
        board.getBuildingDeck().addCardToTopRow(board, this.getGameState().getCurrentEra());
    }

    /**
     * Executes end-of-round cleanup and draws new cards for the next round.
     */
    public void endRoundOperations() {
        // 1. Discard all Characters and EventCards in the bottom row (BuildingCards stay).
        board.getBottomRow().removeIf(card -> card.isCharacter() || card.isEventCard());

        // 2. Move remaining Character and Event cards from the top row to the bottom row (left of BuildingCards).
        List<Card> cardsToMove = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card.isCharacter() || card.isEventCard()) {
                cardsToMove.add(card);
                return true; // remove from top row
            }
            return false;
        });

        board.getBottomRow().addAll(0, cardsToMove);

        // 3. Restore the top row to players.size() + 4 cards (to the left of BuildingCards).
        for (int i = 0; i < this.getPlayers().size() + 4; i++) {
            Card drawnCard = board.getMainDeck().draw();
            board.getTopRow().addFirst(drawnCard); // add new card to the left of the top row
            if (drawnCard.getEra() != this.getGameState().getCurrentEra()) {
                this.getGameState().advanceCurrentEra();
                newEraOperations();
            }
        }

    }

    /**
     * Resolves all bottom-row event cards following rule priority.
     */
    public void resolveBottomEvents() {
        // Resolve bottom-row events (priority as in the rules).
        List<Sustenance> sustenanceCards = new ArrayList<>();
        for (Card card : board.getBottomRow()) {
            if (card.isEventCard() && !card.isSustenance()) {
                ((EventCard) card).applyEvent(this);
            } else if (card.isSustenance()) {
                sustenanceCards.add((Sustenance) card);
            }
        }

        for (Sustenance s : sustenanceCards) {
            s.applyEvent(this);
        }
    }

    /**
     * Places a player's totem on the chosen offer tile and removes it from turn order.
     */
    public void placeTotemOnOfferTile(Player player, int tile) {
        // 1. Place the player's totem on the selected offer tile.
        board.getOfferTrack().get(tile-1).placePlayer(player);

        // 2. Remove the player's totem from the turn order tile.
        for(PlayerSlot slot : board.getTurnOrderTile().getSlots()) {
            if( slot.getPlayer() != null && slot.getPlayer().equals(player)) {
                slot.removeTotem();
                break;
            }
        }
    }

    /**
     * Applies the action of the selected offer tile using the given card IDs.
     * The cards are provided as a comma-separated string: "ID1, ID2".
     */
    public void offerTileAction(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
        List<Integer> ids = new ArrayList<>(extractIntegers(cards));

        // Get the offer tile the player is on.
        OfferTile selectedTile = board.getOfferTrack().stream()
                .filter(tile -> tile.getPlayer() != null )
                .filter(tile -> tile.getPlayer().equals(player))
                .findFirst()
                .orElseThrow( () -> new IllegalStateException( "player not found on offerTrack") );

        OfferEffect effect = selectedTile.getOfferEffect();

        // Apply the effect using the registry to find the correct action
        offerActionRegistry.getActionByEffect(effect).execute(this, player, ids);

        // After applying the effect, insert the player on the first available slot of the turn order tile and in case apply the effect of the slot
        board.getTurnOrderTile().getSlots().stream()
                .filter(slot -> slot.getPlayer() == null )
                .findFirst()
                .ifPresentOrElse(
                        slot -> slot.placePlayerAndApplyEffect(player),
                        () -> {throw new IllegalStateException("No slot available"); }
                );


        // Check if a player has the RoundFlowTotemBC to apply the bonus effect.
        getBoard().getTurnOrderTile().getSlots().stream()
                .filter(slot -> slot.getPlayer() != null && slot.getFood() > 0)
                .filter(slot -> slot.getPlayer().getOwnedBuildings().stream().anyMatch(b -> b instanceof RoundFlowTotemBC))
                .findFirst()
                .ifPresent(slot -> {
                    // At this point the slot exists and has the card.
                    RoundFlowTotemBC totem = (RoundFlowTotemBC) slot.getPlayer().getOwnedBuildings().stream()
                            .filter(b -> b instanceof RoundFlowTotemBC)
                            .findFirst()
                            .orElseThrow();

                    totem.applyEffect(slot.getPlayer(), this);
                });

        // Now the player is already on the turn order tile, we can remove it from the offer tile
        selectedTile.removePlayer();
    }

    /**
     * Executes the RoundFlow extra draw from the top row using the given card IDs.
     */
    public void roundFlowCardRequest(Player player, String cards) throws NoDrawableCardException, InvalidCardException {
        List<Integer> ids = new ArrayList<>(extractIntegers(cards));
        offerActionRegistry.getActionByEffect(OfferEffect.U).execute(this, player, ids);
    }

    /**
     * Utility method to extract integers from a comma-separated string.
     */
    private List<Integer> extractIntegers(String inputString) {
        List<Integer> numbers = new ArrayList<>();

        inputString = inputString.trim();

        // Check for empty input.
        if ( inputString.isEmpty() ) {
            return numbers;
        }

        // Split the string.
        String[] IDs = inputString.split(",");

        // Convert to integers.
        for (String ID : IDs) {
            try {
                numbers.add(Integer.parseInt(ID.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID string not valid: " + ID, e);
            }
        }

        for (int i = 1; i < numbers.size(); i++) {
            for (int j = 0; j < i; j++) {
                if ( numbers.get(i).equals(numbers.get(j)) ) {
                    throw new IllegalArgumentException("Can't insert duplicate ID");
                }
            }
        }


        return numbers;
    }


    /**
     * Resolves end-game events, computes final points, and sets winners.
     */
    public void endOfGame() {
        // 1. Resolve all visible events (top and bottom row).
        List<Sustenance> sustenanceCards = new ArrayList<>();
        for (Card card : board.getBottomRow()) {
            if (card.isEventCard() && !card.isSustenance()) {
                ((EventCard) card).applyEvent(this);
            } else if (card.isSustenance()) {
                sustenanceCards.add((Sustenance) card);
            }
        }

        for (Card card : board.getTopRow()) {
            if (card.isEventCard() && !card.isSustenance()) {
                ((EventCard) card).applyEvent(this);
            } else if (card.isSustenance()) {
                sustenanceCards.add((Sustenance) card);
            }
        }

        for (Sustenance s : sustenanceCards) {
            s.applyEvent(this);
        }


        // 2. Sum up Prestige points gained during the game.
        for (Player p : players) {

            // Add Builders points.
            for (Builder build : p.getBuilders()) {
                p.addPoints(build.getEndPoints());
            }

            // Add Inventors points.
            int numInventors = p.getInventors().size();
            int numInventions = (int) p.getInventors().stream()
                    .map(Inventor::getInvention)
                    .distinct()
                    .count();
            p.addPoints(numInventions * numInventors);

            // Add Artists points.
            int numArtistsPoints = (p.getArtists().size() / 2) * 10;
            p.addPoints( numArtistsPoints);

            // Add Building cards points and end-game effects.
            for ( BuildingCard building : p.getOwnedBuildings() ) {
                p.addPoints( building.getEndPoints() );

                // Apply end-game building effects.
                if ( building.isEndGameBuilding() ) {
                    building.applyEffect(p, this);
                }
            }

        }

        // 3. Find the winner(s).
        Comparator<Player> ranking = Comparator
                .comparingInt(Player::getPoints)
                .thenComparingInt(Player::getFood);

        Player best = Collections.max(players, ranking);

        List<Player> winners = players.stream()
                .filter(p -> ranking.compare(p, best) == 0)
                .collect(Collectors.toList());

        gameState.setWinners(winners);


    }
}