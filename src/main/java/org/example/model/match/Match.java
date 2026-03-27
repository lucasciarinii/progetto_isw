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
import org.example.model.cards.characters.Character;
import org.example.model.cards.eventCards.EventCard;
import org.example.model.cards.eventCards.Sustenance;
import org.example.model.enums.OfferEffect;
import org.example.model.interfaces.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        * 11) il primo giocatore ottiene 2 cibi, il secondo e terzo 3, il quarto e quinto 5; ✅
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
        board.getBottomRow().removeIf(card -> card instanceof BuildingCard);

        // 2. Move any building card present in the top row to the bottom row, and place them to the right of the Tribe cards
        List<Card> buildingCardsInTopRow = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card instanceof BuildingCard) {
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
        resolveBottomEvents();

        // 2. Discard all Characters and EventCards in the bottom row (BuildingCards stay)
        board.getBottomRow().removeIf(card -> card instanceof org.example.model.cards.characters.Character || card instanceof EventCard);

        // 3. Move all remaining Character and event cards from the top row to the bottom row (at the left of the BuildingCards) (BuildingCards stay in the top row)
        List<Card> cardsToMove = new ArrayList<>();
        board.getTopRow().removeIf(card -> {
            if (card instanceof Character || card instanceof EventCard) {
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

        // 5. Replace Totems in correct order on turnOrderTile and removing from offerTrack
        for (int i = 0; i < this.getPlayers().size(); i++)
        {
            board.getTurnOrderTile().getSlots().get(i).setPlayer(board.getOfferTrack().get(i).getPlayer());
            board.getOfferTrack().get(i).removePlayer();
        }
    }

    private void resolveBottomEvents() {
        // Resolve events of bottomRow (with priority as in the rules)
        List<Sustenance> sustenance_cards = new ArrayList<>();
        for (Card card : board.getBottomRow()) {
            if (card instanceof EventCard && !(card instanceof Sustenance)) {
                ((EventCard) card).applyEvent(this);
            } else if (card instanceof Sustenance) {
                sustenance_cards.add((Sustenance) card);
            }
        }

        for (Sustenance s : sustenance_cards) {
            s.applyEvent(this);
        }
    }

    private void resolveTopEvents() {
        // Resolve events of bottomRow (with priority as in the rules)
        List<Sustenance> sustenance_cards = new ArrayList<>();
        for (Card card : board.getTopRow()) {
            if (card instanceof EventCard && !(card instanceof Sustenance)) {
                ((EventCard) card).applyEvent(this);
            } else if (card instanceof Sustenance) {
                sustenance_cards.add((Sustenance) card);
            }
        }

        for (Sustenance s : sustenance_cards) {
            s.applyEvent(this);
        }
    }


    public void placeTotemOnOfferTile(Player p, int tile) {
        board.getOfferTrack().get(tile-1).placePlayer(p);
        for (int i = 0; i < this.getPlayers().size(); i++)
        {
            if (board.getTurnOrderTile().getSlots().get(i).getPlayer()==p)
            {
                board.getTurnOrderTile().getSlots().get(i).removeTotem();
                break;
            }
        }

    }

    //the cards the user selects are all in one string "ID1, ID2, ID3"
    public void offerTileAction(Player p, String cards){
        OfferEffect effect=null;
        boolean found=false;
        List<Integer> numeri = new ArrayList<>(estraiInteriDaStringa(cards));
        for(OfferTile S :board.getOfferTrack())
        {
            if (S.getPlayer()==p)
            {
                effect = S.getOfferEffect();
            }
        }
        switch (effect) {
            case FOOD -> p.addFood(3);
            case D -> {
                if(numeri.size()!=1)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for (int i = 0; i < board.getBottomRow().size(); i++)
                {
                    if(board.getBottomRow().get(i).getId()==numeri.get(0))
                    {
                        try{
                            ((Character)board.getBottomRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                            found=true;
                            board.getBottomRow().removeIf(card -> card.getId()==numeri.get(0));
                            break;
                        }
                        catch (IllegalArgumentException e)
                        {
                            System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                        }
                    }


                }
                if(!found) throw new IllegalArgumentException("Invalid string");
            }
            case U -> {
                if(numeri.size()!=1)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for (int i = 0; i < board.getTopRow().size(); i++)
                {
                    if(board.getTopRow().get(i).getId()==numeri.get(0))
                    {
                        try{
                            ((Character)board.getTopRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                            found=true;
                            board.getTopRow().removeIf(card -> card.getId()==numeri.get(0));
                            break;
                        }
                        catch (IllegalArgumentException e)
                        {
                            System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                        }
                    }
                }
                if(!found) throw new IllegalArgumentException("Invalid string");
            }
            case DD -> {
                if(numeri.size()!=2)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for(int a = 0; a < 2; a++)
                {
                    found=false;
                    for (int i = 0; i < board.getBottomRow().size(); i++)
                    {
                        if(board.getBottomRow().get(i).getId()==numeri.get(a))
                        {
                            try{
                                ((Character)board.getBottomRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                found=true;
                                break;
                            }
                            catch (IllegalArgumentException e)
                            {
                                System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                            }
                        }
                    }
                    if(!found) throw new IllegalArgumentException("Invalid string");
                }
                if(found)
                {
                    board.getBottomRow().removeIf(card -> card.getId()==numeri.get(0));
                    board.getBottomRow().removeIf(card -> card.getId()==numeri.get(1));
                }
            }
            case DU -> {
                if(numeri.size()!=2)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for(int a = 0; a < 2; a++)
                {
                    found=false;
                    if(a==0)
                    {
                        for (int i = 0; i < board.getBottomRow().size(); i++)
                        {
                            if(board.getBottomRow().get(i).getId()==numeri.get(a))
                            {
                                try{
                                    ((Character)board.getBottomRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                    found=true;
                                    break;
                                }
                                catch (IllegalArgumentException e)
                                {
                                    System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                                }
                            }
                        }
                    }
                    else
                    {
                        for (int i = 0; i < board.getTopRow().size(); i++)
                        {
                            if(board.getTopRow().get(i).getId()==numeri.get(a))
                            {
                                try{
                                    ((Character)board.getTopRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                    found=true;
                                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(1));
                                    break;
                                }
                                catch (IllegalArgumentException e)
                                {
                                    System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                                }
                            }
                        }
                    }
                    if(!found) throw new IllegalArgumentException("Invalid string");
                    else board.getTopRow().removeIf(card -> card.getId()==numeri.get(0));

                }
            }
            case UU -> {
                if(numeri.size()!=2)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for(int a = 0; a < 2; a++)
                {
                    found=false;
                    for (int i = 0; i < board.getTopRow().size(); i++)
                    {
                        if(board.getTopRow().get(i).getId()==numeri.get(a))
                        {
                            try{
                                ((Character)board.getTopRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                found=true;
                                break;
                            }
                            catch (IllegalArgumentException e)
                            {
                                System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                            }
                        }
                    }
                    if(!found) throw new IllegalArgumentException("Invalid string");
                }
                if(found)
                {
                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(0));
                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(1));
                }
            }
            case DUU -> {
                if(numeri.size()!=3)
                {
                    throw new IllegalArgumentException("Invalid string");
                }
                for(int a = 0; a < 3; a++)
                {
                    found=false;
                    if(a==0)
                    {
                        for (int i = 0; i < board.getBottomRow().size(); i++)
                        {
                            if(board.getBottomRow().get(i).getId()==numeri.get(a))
                            {
                                try{
                                    ((Character)board.getBottomRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                    found=true;
                                    break;
                                }
                                catch (IllegalArgumentException e)
                                {
                                    System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                                }
                            }
                        }
                    }
                    else
                    {
                        for (int i = 0; i < board.getTopRow().size(); i++)
                        {
                            if(board.getTopRow().get(i).getId()==numeri.get(a))
                            {
                                try{
                                    ((Character)board.getTopRow().get(i)).accept((Visitor)p); //verificare se aggiungere implements Visitor in Player
                                    found=true;
                                    break;
                                }
                                catch (IllegalArgumentException e)
                                {
                                    System.out.println("Errore: l'oggetto non è un Character o il Player non è un Visitor!");
                                }
                            }
                        }
                    }
                    if(!found) throw new IllegalArgumentException("Invalid string");
                }
                if (found)
                {
                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(0));
                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(1));
                    board.getTopRow().removeIf(card -> card.getId()==numeri.get(2));
                }
            }
            default -> throw new IllegalArgumentException("Invalid player");
        }

    }
    public static List<Integer> estraiInteriDaStringa(String stringaInput) {
        List<Integer> numeri = new ArrayList<>();

        // Controlla se la stringa è nulla o vuota per evitare errori
        if (stringaInput == null || stringaInput.trim().isEmpty()) {
            return numeri; // Ritorna una lista vuota
        }

        // 1. Divide la stringa usando la virgola come delimitatore
        String[] parti = stringaInput.split(",");

        // 2. Itera su ogni parte estratta
        for (String parte : parti) {
            try {
                // 3. Rimuove spazi bianchi e converte la stringa in intero
                int numero = Integer.parseInt(parte.trim());
                numeri.add(numero);
            } catch (NumberFormatException e) {
                // Ignora le parti che non sono numeri interi validi
                System.err.println("Impossibile convertire '" + parte + "' in un numero. Inserirne un altro valido.");
            }
        }

        return numeri;
    }

    // TODO:
    public void endOfGame() {
    }


}
