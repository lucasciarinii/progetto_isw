package it.polimi.ingsw.server.model.board;

import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.decks.BuildingDeck;
import it.polimi.ingsw.server.model.decks.MainDeck;
import it.polimi.ingsw.server.model.enums.OfferEffect;
import it.polimi.ingsw.server.model.match.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Game board containing the offer track, decks, and card rows.
 */
public class Board {
    /** Turn order tile with player slots. */
    private final TurnOrderTile turnOrderTile;
    /** Offer track tiles. */
    private final List<OfferTile> offerTrack;
    /** Main deck with character and event cards. */
    private final MainDeck mainDeck;
    /** Building deck used to populate the top row. */
    private final BuildingDeck buildingDeck;
    /** Visible cards on the top row. */
    private final List<Card> topRow;
    /** Visible cards on the bottom row. */
    private final List<Card> bottomRow;

    /**
     * Creates the board and initializes rows and decks based on players.
     *
     * @param players list of players in the match
     */
    public Board(List<Player> players) {
        Objects.requireNonNull(players, "Players list cannot be null");

        if ( players.contains(null) ) {
            throw new NullPointerException("Players can't be null");
        }

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

        // bottomRow and topRow instantiation and initialization
        topRow = new ArrayList<>();
        bottomRow = new ArrayList<>();

        // buildingDeck initialization, based on the number of players (delegated to BuildingDeck constructor)
        buildingDeck = new BuildingDeck(players.size(), this);

        // bottomRow initialization: draw cards until we have players.size() + 1 cards in the bottom row, if we draw an event card, put it in the top row (and continue to draw until we have enough cards in the bottom row)
        for (int i = 0; i < players.size() + 1; i++) {
            Card drawnCard = mainDeck.draw();
            if (drawnCard != null && drawnCard.isEventCard()) {
                topRow.addFirst(drawnCard);
                i--; // decrement i to draw another card for the bottom row
            } else
                bottomRow.add(drawnCard);
        }

        // topRow initialization: draw cards until we have players.size() + 4 cards in the top row (taking into account the event cards we might have drawn in the previous step)

        long numberOfBuildings = topRow.stream().filter(Card::isBuilding).count();
        int targetSize = players.size() + 4;
        int nonBuildingCards = topRow.size() - (int)numberOfBuildings;
        int cardsToDraw = targetSize - nonBuildingCards;

        for (int i = 0; i < cardsToDraw; i++) {
            Card drawn = mainDeck.draw();
            if (drawn != null) {
                topRow.addFirst(drawn);
            } else {
                break; // deck finished
            }
        }
    }

    /**
     * @return the turn order tile
     */
    public TurnOrderTile getTurnOrderTile() {
        return turnOrderTile;
    }

    /**
     * @return the offer track tiles
     */
    public List<OfferTile> getOfferTrack() {
        return offerTrack;
    }

    /**
     * @return the main deck
     */
    public MainDeck getMainDeck() {
        return mainDeck;
    }

    /**
     * @return the building deck
     */
    public BuildingDeck getBuildingDeck() {
        return buildingDeck;
    }

    /**
     * @return the top row cards
     */
    public List<Card> getTopRow() {
        return topRow;
    }

    /**
     * @return the bottom row cards
     */
    public List<Card> getBottomRow() {
        return bottomRow;
    }


}
