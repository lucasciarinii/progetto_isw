package org.example.model.decks;

import org.example.model.cards.buildingCards.BuildingCard;

public class BuildingDeck extends Deck<BuildingCard> {


    public BuildingDeck() {

    }


    public BuildingCard draw(BuildingCard buildingCard) {

        return buildingCard;
    }




}