package org.example.model.match;

import org.example.model.board.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Context {

    private final Board board;
    private final List<Player> players;

    public Context(Board board, ArrayList<Player> players) {
        this.board = board;
        this.players = Collections.unmodifiableList(players);
    }

    public Board getBoard() { return board; }

    public List<Player> getPlayers() { return players; }


}