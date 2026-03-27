package org.example.model.board;

import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.characters.Character;
import org.example.model.cards.characters.Inventor;
import org.example.model.cards.eventCards.EventCard;
import org.example.model.cards.eventCards.Sustenance;
import org.example.model.decks.BuildingDeck;
import org.example.model.decks.MainDeck;
import org.example.model.enums.Era;
import org.example.model.enums.OfferEffect;
import org.example.model.interfaces.Visitor;
import org.example.model.match.Match;
import org.example.model.match.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Board {
    private final TurnOrderTile turnOrderTile;
    private final List<OfferTile> offerTrack;
    private final MainDeck mainDeck;
    private final BuildingDeck buildingDeck;
    private final List<Card> topRow;
    private final List<Card> bottomRow;

    public Board(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");

        // turnOrderTile initialization (delegated to TurnOrderTile constructor)
        turnOrderTile = new TurnOrderTile(players);

        // offerTrack initialization
        switch (players.size()) {
            case 2 ->
                    offerTrack = List.of(new OfferTile(OfferEffect.D), new OfferTile(OfferEffect.U), new OfferTile(OfferEffect.DU), new OfferTile(OfferEffect.UU));
            case 3 ->
                    offerTrack = List.of(new OfferTile(OfferEffect.D), new OfferTile(OfferEffect.U), new OfferTile(OfferEffect.DD), new OfferTile(OfferEffect.DU), new OfferTile(OfferEffect.UU));
            case 4 ->
                    offerTrack = List.of(new OfferTile(OfferEffect.D), new OfferTile(OfferEffect.U), new OfferTile(OfferEffect.DD), new OfferTile(OfferEffect.DU), new OfferTile(OfferEffect.UU), new OfferTile(OfferEffect.DUU));
            case 5 ->
                    offerTrack = List.of(new OfferTile(OfferEffect.FOOD), new OfferTile(OfferEffect.D), new OfferTile(OfferEffect.U), new OfferTile(OfferEffect.DD), new OfferTile(OfferEffect.DU), new OfferTile(OfferEffect.UU), new OfferTile(OfferEffect.DUU));
            default -> throw new IllegalArgumentException("Invalid list of players");
        }

        // mainDeck initialization, based on the number of players (delegated to MainDeck constructor)
        mainDeck = new MainDeck(players.size());

        // buildingDeck initialization, based on the number of players (delegated to BuildingDeck constructor)
        buildingDeck = new BuildingDeck(players.size(), this);

        // bottomRow and topRow instantiation and initialization
        topRow = new ArrayList<>();
        bottomRow = new ArrayList<>();

        // bottomRow initialization: draw cards until we have players.size() + 1 cards in the bottom row, if we draw an event card, put it in the top row (and continue to draw until we have enough cards in the bottom row)
        for (int i = 0; i < players.size() + 1; i++) {
            Card drawnCard = mainDeck.draw();
            if (drawnCard instanceof EventCard) {
                topRow.add(drawnCard);
                i--; // decrement i to draw another card for the bottom row
            } else
                bottomRow.add(drawnCard);
        }

        // topRow initialization: draw cards until we have players.size() + 4 cards in the top row (taking into account the event cards we might have drawn in the previous step)
        for (int i = 0; i < players.size() + 4 - topRow.size(); i++)
            topRow.add(mainDeck.draw());

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

