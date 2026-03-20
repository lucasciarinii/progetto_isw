/*
Match rappresenta la classe di una singola partita. Tra i suoi compiti dovrebbe:
- Tenere traccia dei giocatori, del tabellone e dello stato di gioco (players, board, gameState)
- Avviare la partita con un metodo init() che segue i passaggi descritti nella traccia, come la creazione del tracciato, la distribuzione delle carte, l'assegnazione dei totem e dei cibi iniziali.
- Essere il punto di accesso unico per il controller
*/
package org.example.model.match;

import org.example.model.board.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Match {

    private final List<Player> players;

    private Board board;

    private GameState gameState;

    public Match(List<Player> players) {
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

}
