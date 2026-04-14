/*
Match rappresenta la classe di una singola partita. Tra i suoi compiti dovrebbe:
- Tenere traccia dei giocatori, del tabellone e dello stato di gioco (players, board, gameState)
- Avviare la partita con un metodo init() che segue i passaggi descritti nella traccia, come la creazione del tracciato, la distribuzione delle carte, l'assegnazione dei totem e dei cibi iniziali.
- Essere il punto di accesso unico per il controller
*/
package org.example.model.match;

import org.example.model.board.Board;
import org.example.model.board.OfferTile;
import org.example.model.board.PlayerSlot;
import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.characters.Builder;
import org.example.model.cards.characters.Inventor;
import org.example.model.cards.eventCards.EventCard;
import org.example.model.cards.eventCards.Sustenance;
import org.example.model.enums.OfferEffect;

import java.util.*;
import java.util.stream.Collectors;

public class Match {

    private final List<Player> players;

    private Board board;

    private GameState gameState;

    public Match(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");
        this.players = new ArrayList<>(players);

        init(); // initialize board and gameState
    }
    public List<Player> getPlayers(){return Collections.unmodifiableList(players);}

    public Board getBoard(){return board;}

    public GameState getGameState(){return gameState;}

    private void init() { // should initialize the board and gameState according to the steps
        /*
        * 1) creare il tracciato con le carte corrispondenti ✅
        * al numero di giocatori(le OfferTile; ✅
        * 2) scegliere la tessera TurnOrderTile in base al numero di giocatori; ✅
        * 3) inizializzare i mazzi delle carte tribù in base al numero di giocatori
        * e distribuirle in 3 mazzi in base all'era e mischiamo i mazzi ✅
        * 4) si impilano i mazzi partendo dal fondo con le carte evento finale,
        * poi era 3, 2 e 1; ✅
        * 5) pescare le carte dal mazzo tribù un numero di carte pari al N di
        * giocatori +1 e forma la fila inferiore, se peschi una carta evento va
        * messa nella fila superiore e continui a pescare fino a completare la fila sotto ✅
        * 6) poi pesco un numero di carte pari ai giocatori più 4 e formo la fila superiore
        * (a queste devi tenere conto delle carte evento che avevi aggiunto nella fase prima) ✅
        * 7) creo e mescolo i mazzi degli edifici divisi per ere in base al numero di giocatori ✅
        * 8) porre a destra della fila superiore tutte le carte edificio dell'era I ‼️
        * 9) ogni giocatore riceve un totem e una scheda riassuntiva ‼️
        * 10) si piazzano i totem in ordine casuale sulla carta OrderTile ✅
        * 11) il primo giocatore ottiene 2 cibi, il secondo e terzo 3, il quarto e quinto 4; ✅
        * 12) ogni partita dura 10 round composto da due fasi: scegliere la carta del tracciato e risolvere;
        * 13) a partire dall'alto i giocatori scelgono la carta del tracciato devo mettersi;
        * 14) a partire da sinistra il giocatore risolve l'azione della carta;
        * 15) in base alla carta puoi o prendere solo cibo o scegliere uno o più personaggi;
        * 16) quando prendi le carte personaggio le aggiungi  alla tua lista delle carte;
        * 17) azione per i tipi di personaggi da verificare nel corso della partita;
        * 18)
        * */

        // Passo 9-10: randomizes players order
        Collections.shuffle(players);

        // Steps 1-8: delegate everything to Board (but first I must have the players already shuffled, because Board logic uses the already randomized player list
        board = new Board(players);

        // Passo 11: distribute food based on player order
        for (int i = 0; i < players.size(); i++) {
            int food = switch (i) {
                case 0 -> 2;
                case 1, 2 -> 3;
                case 3, 4 -> 4;
                default -> throw new IllegalArgumentException("Invalid list of players");
            };
            players.get(i).addFood(food);
        }

        // Initialize GameState (with random order already done)
        gameState = new GameState(players);
    }

    //! METHODS TO MANAGE THE MATCH

    // Invocated at the beginning of each new Era
    public void newEraOperations() {
        // 1. discard any building cards present in the bottom row
        board.getBottomRow().removeIf(Card::isBuilding);

        // 2. Move any building card present in the top row to the bottom row, and place them to the right of the Tribe cards
        List<Card> buildingCardsInTopRow = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card.isBuilding()) {
                buildingCardsInTopRow.add(card);
                return true; // remove from top row
            }
            return false; // keep in top row
        });
        board.getBottomRow().addAll(buildingCardsInTopRow);

        // 3. Place the Building card from the just-started Era in the top row to the right of the Tribe cards, face up
        board.getBuildingDeck().addCardToTopRow(board, this.getGameState().getCurrentEra()); // the getCurrentEra() should be already updated
    }

    // Invocated at the end of each round, after all players have resolved their actions and before starting a new round
    public void endRoundOperations() {
        // 1. Resolve events of bottomRow (with priority as in the rules)
        // resolveBottomEvents(); this is a separate PHASE

        // 2. Discard all Characters and EventCards in the bottom row (BuildingCards stay)
        board.getBottomRow().removeIf(card -> card.isCharacter() || card.isEventCard());

        // 3. Move all remaining Character and event cards from the top row to the bottom row (at the left of the BuildingCards) (BuildingCards stay in the top row)
        List<Card> cardsToMove = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card.isCharacter() || card.isEventCard()) {
                cardsToMove.add(card);
                return true; // Rimuove dalla topRow
            }
            return false;
        });

        board.getBottomRow().addAll(0, cardsToMove);

        // 4. Restore the topRow to the number of cards equal to players.size() + 4 (at the left of the BuildingCards)
        for (int i = 0; i < this.getPlayers().size() + 4; i++) {
            Card drawnCard = board.getMainDeck().draw();
            board.getTopRow().add(0, drawnCard); // add new card to the left of the top row
            if (drawnCard.getEra() != this.getGameState().getCurrentEra()) { // true means that we have drawn a card of a new era, so we need to update the current era in the GameState
                this.getGameState().advanceCurrentEra(); // update the current era in the GameState
            }
        }
    }

    public void resolveBottomEvents() {
        // Resolve events of bottomRow (with priority as in the rules)
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

//    private void resolveTopEvents() {
//        // Resolve events of topRow (with priority as in the rules)
//        List<Sustenance> sustenanceCards = new ArrayList<>();
//        for (Card card : board.getTopRow()) {
//            if (card.isEventCard() && !card.isSustenance()) {
//                ((EventCard) card).applyEvent(this);
//            } else if (card.isSustenance()) {
//                sustenanceCards.add((Sustenance) card);
//            }
//        }
//
//        for (Sustenance s : sustenanceCards) {
//            s.applyEvent(this);
//        }
//    }


    public void placeTotemOnOfferTile(Player player, int tile) {
        // 1. Place the player's totem on the selected offer tile
        board.getOfferTrack().get(tile-1).placePlayer(player);

        // 2. Remove the player's totem from the turn order tile
        for(PlayerSlot slot : board.getTurnOrderTile().getSlots()) {
            try {
                 if(slot.getPlayer().equals(player)) {
                    slot.removeTotem();
                    break;
                }
            }
            catch (NullPointerException e) {

            }
        }
    }

    //the cards the user selects are all in one string "ID1, ID2, ID3"
    public void offerTileAction(Player player, String cards) {
        List<Integer> numbers = new ArrayList<>(extractIntegers(cards));

        OfferTile selectedTile = board.getOfferTrack().stream()
                .filter(tile -> tile.getPlayer() != null )
                .filter(tile -> tile.getPlayer().equals(player))
                .findFirst()
                .orElseThrow( () -> new IllegalArgumentException( "player not found on offerTrack") );

        OfferEffect effect = selectedTile.getOfferEffect();

        if (effect == null) {
            throw new NullPointerException("effect can't be null");
        }


        switch (effect) {

            // Add food effect
            case FOOD -> player.addFood(3);

            // Choose one card from bottomRow
            case D -> {

                // player must select only one ID card
                if (numbers.size() != 1) {
                    throw new IllegalArgumentException("Invalid String: player must select only 1 card");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getBottomRow().isEmpty()) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the card with corresponding ID
                Card card = board.getBottomRow().stream()
                        .filter(c -> c.getId() == numbers.get(0))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID card") );

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (card.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) card;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getBottomRow().remove(buildingCard);
                        return;
                    }
                }

                // add card to player
                player.acceptCard(card);

                // remove card from bottomRow
                board.getBottomRow().remove(card);
            }

            // Choose one card from topRow
            case U -> {
                // player must select only one ID card
                if (numbers.size() != 1) {
                    throw new IllegalArgumentException("Invalid String: player must select only 1 card");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getTopRow().isEmpty()) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the card with corresponding ID
                Card card = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(0))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID card") );

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (card.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) card;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        return;
                    }
                }

                // add card to player
                player.acceptCard(card);

                // remove card from topRow
                board.getTopRow().remove(card);
            }

            case DD -> {
                // player must select exactly two IDs from cards
                if (numbers.size() != 2) {
                    throw new IllegalArgumentException("Invalid String: player must select exactly 2 IDs from cards");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getBottomRow().isEmpty()) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the cards with corresponding IDs
                List<Card> cards_input = board.getBottomRow().stream()
                        .filter(c -> (c.getId() == numbers.get(0) || c.getId() == numbers.get(1)))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .collect(Collectors.toList());

                // if cards_input.size() != 2 means that at least one of the two selected IDs is invalid (not present in the bottom row or not a Character card)
                if (cards_input.size() != 2) {
                    throw new IllegalArgumentException("Invalid ID cards");
                }

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (cards_input.get(0).isBuilding() && cards_input.get(1).isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) cards_input.get(0);
                    BuildingCard buildingCard1 = (BuildingCard) cards_input.get(1);

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getBottomRow().remove(buildingCard0);
                        board.getBottomRow().remove(buildingCard1);
                        return;
                    }
                }
                if (cards_input.get(0).isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) cards_input.get(0);
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getBottomRow().remove(buildingCard);
                        player.acceptCard(cards_input.get(1));
                        board.getBottomRow().remove(cards_input.get(1));
                        return;
                    }
                }
                if (cards_input.get(1).isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) cards_input.get(1);
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getBottomRow().remove(buildingCard);
                        player.acceptCard(cards_input.get(0));
                        board.getBottomRow().remove(cards_input.get(0));
                        return;
                    }
                }

                // add cards to player
                player.acceptCard(cards_input.get(0));
                player.acceptCard(cards_input.get(1));

                // remove card from bottomRow
                board.getBottomRow().remove(cards_input.get(0));
                board.getBottomRow().remove(cards_input.get(1));
            }

            case DU -> {

                // player must select exactly two IDs from cards
                if (numbers.size() != 2) {
                    throw new IllegalArgumentException("Invalid String: player must select exactly 2 IDs from cards");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getBottomRow().isEmpty() || board.getTopRow().isEmpty() ) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the card with corresponding ID from bottomRow
                Card bottomCard = board.getBottomRow().stream()
                        .filter(c -> c.getId() == numbers.get(0))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

                // find the card with corresponding ID from topRow
                Card topCard = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(1))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (bottomCard.isBuilding() && topCard.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) bottomCard;
                    BuildingCard buildingCard1 = (BuildingCard) topCard;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getBottomRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        return;
                    }
                }
                if (bottomCard.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) bottomCard;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getBottomRow().remove(buildingCard);
                        player.acceptCard(topCard);
                        board.getTopRow().remove(topCard);
                        return;
                    }
                }
                if (topCard.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) topCard;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        player.acceptCard(bottomCard);
                        board.getBottomRow().remove(bottomCard);
                        return;
                    }
                }

                // add cards to player
                player.acceptCard(bottomCard);
                player.acceptCard(topCard);

                // remove card from bottomRow and topRow
                board.getBottomRow().remove(bottomCard);
                board.getTopRow().remove(topCard);

            }

            case UU -> {

                // player must select exactly two IDs from cards
                if (numbers.size() != 2) {
                    throw new IllegalArgumentException("Invalid String: player must select exactly 2 IDs from cards");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getTopRow().isEmpty()) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the card with corresponding ID from bottomRow
                Card topCard1 = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(0))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

                // find the card with corresponding ID from topRow
                Card topCard2 = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(1))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (topCard1.isBuilding() && topCard2.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) topCard1;
                    BuildingCard buildingCard1 = (BuildingCard) topCard2;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getTopRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        return;
                    }
                }
                if (topCard1.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) topCard1;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.addFood(-buildingCard.getFoodCost()); // pay the cost
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        player.acceptCard(topCard2);
                        board.getTopRow().remove(topCard2);
                        return;
                    }
                }
                if (topCard2.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) topCard2;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        player.acceptCard(topCard1);
                        board.getTopRow().remove(topCard1);
                        return;
                    }
                }

                // add cards to player
                player.acceptCard(topCard1);
                player.acceptCard(topCard2);

                // remove cards from topRow
                board.getTopRow().remove(topCard1);
                board.getTopRow().remove(topCard2);

            }
            case DUU -> {

                // player must select exactly three IDs from cards
                if (numbers.size() != 3) {
                    throw new IllegalArgumentException("Invalid String: player must select exactly 3 IDs from cards");
                }

                // if the row does not contain any card at all, an exception will be thrown
                if( board.getBottomRow().isEmpty() || board.getTopRow().isEmpty() ) {
                    throw new IllegalArgumentException("The row is empty, no card can be selected");
                }

                // Find the card with corresponding ID from bottomRow
                Card bottomCard = board.getBottomRow().stream()
                        .filter(c -> c.getId() == numbers.get(0))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

                Card topCard1 = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(1))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID bottomRow card") );

                // find the card with corresponding ID from topRow
                Card topCard2 = board.getTopRow().stream()
                        .filter(c -> c.getId() == numbers.get(2))
                        .filter(c -> c.isCharacter() || c.isBuilding())
                        .findFirst()
                        .orElseThrow( () -> new IllegalArgumentException("invalid ID topRow card") );

                // Check if the card is BuildingCard, in case we have to check if the player can accept it (if he has enough food to pay the cost)
                if (topCard1.isBuilding() && topCard2.isBuilding() && bottomCard.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) topCard1;
                    BuildingCard buildingCard1 = (BuildingCard) topCard2;
                    BuildingCard buildingCard2 = (BuildingCard) bottomCard;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() + buildingCard2.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() - buildingCard2.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        player.acceptCard(buildingCard2);
                        board.getTopRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        board.getBottomRow().remove(buildingCard2);
                        return;
                    }
                }
                if (bottomCard.isBuilding() && topCard1.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) bottomCard;
                    BuildingCard buildingCard1 = (BuildingCard) topCard1;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getBottomRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        player.acceptCard(topCard2);
                        board.getTopRow().remove(topCard2);
                        return;
                    }
                }
                if (bottomCard.isBuilding() && topCard2.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) bottomCard;
                    BuildingCard buildingCard1 = (BuildingCard) topCard2;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getBottomRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        player.acceptCard(topCard1);
                        board.getTopRow().remove(topCard1);
                        return;
                    }
                }
                if (topCard1.isBuilding() && topCard2.isBuilding()) {
                    BuildingCard buildingCard0 = (BuildingCard) topCard1;
                    BuildingCard buildingCard1 = (BuildingCard) topCard2;

                    // Check if the player has enough food to take both building cards
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard0.getFoodCost() + buildingCard1.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take these building cards");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard0.getFoodCost() - buildingCard1.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard0);
                        player.acceptCard(buildingCard1);
                        board.getTopRow().remove(buildingCard0);
                        board.getTopRow().remove(buildingCard1);
                        player.acceptCard(bottomCard);
                        board.getBottomRow().remove(bottomCard);
                        return;
                    }
                }
                if (topCard1.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) topCard1;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        player.acceptCard(topCard2);
                        board.getTopRow().remove(topCard2);
                        player.acceptCard(bottomCard);
                        board.getBottomRow().remove(bottomCard);
                        return;
                    }
                }
                if (topCard2.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) topCard2;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getTopRow().remove(buildingCard);
                        player.acceptCard(topCard1);
                        board.getTopRow().remove(topCard1);
                        player.acceptCard(bottomCard);
                        board.getBottomRow().remove(bottomCard);
                        return;
                    }
                }
                if (bottomCard.isBuilding()) {
                    BuildingCard buildingCard = (BuildingCard) bottomCard;
                    if ( player.getFood() + player.getDiscountOnBuilding() < buildingCard.getFoodCost() ) {
                        throw new IllegalArgumentException("Player doesn't have enough food to take this building card");
                    }
                    else {
                        player.addFood(Math.min(0, -buildingCard.getFoodCost() + player.getDiscountOnBuilding())); // pay the cost (taking into account the discount on building)
                        player.acceptCard(buildingCard);
                        board.getBottomRow().remove(buildingCard);
                        player.acceptCard(topCard1);
                        board.getTopRow().remove(topCard1);
                        player.acceptCard(topCard2);
                        board.getTopRow().remove(topCard2);
                        return;
                    }
                }

                // add cards to player
                player.acceptCard(bottomCard);
                player.acceptCard(topCard1);
                player.acceptCard(topCard2);

                // remove cards from topRow and bottomRow
                board.getBottomRow().remove(bottomCard);
                board.getTopRow().remove(topCard1);
                board.getTopRow().remove(topCard2);

            }
            default -> throw new IllegalArgumentException("Unknown or unsupported OfferEffect");
        }

        board.getTurnOrderTile().getSlots().stream()
                .filter(slot -> slot.getPlayer() == null )
                .findFirst()
                .ifPresentOrElse(
                        slot -> slot.placePlayerAndApplyEffect(player),
                        () -> {throw new IllegalStateException("No slot available"); }
                );

        selectedTile.removePlayer();

    }

    private List<Integer> extractIntegers(String inputString) {
        List<Integer> numbers = new ArrayList<>();

        inputString = inputString.trim();

        // Check if null string or empty string
        if ( inputString.isEmpty() ) {
            return numbers;
        }

        // split the string
        String[] IDs = inputString.split(",");

        // convert string into array of integers
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


    public void endOfGame() {
        // 1. Solve all the visible events (top and bottom row)
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


        // 2. add up Prestige points gained during the game
        for (Player p : players) {

            // add Builders points
            for (Builder build : p.getBuilders()) {
                p.addPoints(build.getEndPoints());
            }

            // add Inventors points
            int numInventors = p.getInventors().size();
            int numInventions = (int) p.getInventors().stream()
                    .map(Inventor::getInvention)
                    .distinct()
                    .count();
            p.addPoints(numInventions * numInventors);

            // add Artists points
            int numArtistsPoints = (p.getArtists().size() / 2) * 10;
            p.addPoints( numArtistsPoints);

            // add Building Cards points
            for ( BuildingCard building : p.getOwnedBuildings() ) {
                p.addPoints( building.getEndPoints() );

                // apply end game building events
                if ( building.isEndGameBuilding() ) {
                    building.applyEffect(p, this);
                }
            }

        }

        // 3. find the winner(s)
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