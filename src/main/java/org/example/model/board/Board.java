package org.example.model.board;

import org.example.model.cards.Card;
import org.example.model.decks.BuildingDeck;
import org.example.model.decks.MainDeck;

import java.util.List;

public class Board {
    private final TurnOrderTile turnOrderTile;
    private final List<OfferTile> offerTrack;
    private final MainDeck mainDeck;
    private final BuildingDeck buildingDeck;
    private final List<Card> topRow;
    private final List<Card> bottomRow;

    public Board(TurnOrderTile turnOrderTile, List<OfferTile> offerTrack, MainDeck mainDeck, BuildingDeck buildingDeck, List<Card> topRow, List<Card> bottomRow) {
        this.turnOrderTile = turnOrderTile;
        this.offerTrack = offerTrack;
        this.mainDeck = mainDeck;
        this.buildingDeck = buildingDeck;
        this.topRow = topRow;
        this.bottomRow = bottomRow;
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
