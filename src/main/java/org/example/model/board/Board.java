package org.example.model.board;

import org.example.model.cards.Card;
import org.example.model.decks.BuildingDeck;
import org.example.model.decks.MainDeck;

import java.util.List;

public class Board {
    private TurnOrderTile turnOrderTile;
    private List<OfferTile> offerTrack;
    private MainDeck mainDeck;
    private BuildingDeck buildingDeck;
    private List<Card> topRow;
    private List<Card> bottomRow;

    public Board() {

    }

    public TurnOrderTile getTurnOrderTile() {
        return turnOrderTile;
    }

    public List<OfferTile> getOfferTrack() {
        return offerTrack;
    }

    public MainDeck getMainDeck() {
        return mainDeck;
    }

    public BuildingDeck getBuildingDeck() {
        return buildingDeck;
    }

    public List<Card> getTopRow() {
        return topRow;
    }

    public List<Card> getBottomRow() {
        return bottomRow;
    }


}
