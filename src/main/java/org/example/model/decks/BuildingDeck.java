package org.example.model.decks;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.enums.Era;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class BuildingDeck extends Deck<BuildingCard> {

    public BuildingDeck() {
        super();
        initializeDeck();
    }

    public BuildingCard draw(BuildingCard buildingCard) {
        return buildingCard;
    }

    private void initializeDeck() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. Read all objects from the JSON file into a List<Card>
            List<BuildingCard> allCards = mapper.readValue(
                    new File("./decks_json/buildingCards.json"),
                    new TypeReference<List<BuildingCard>>(){}
            );

            // 2. Filter on specific eras and add to the respective lists
            for (BuildingCard card : allCards) {
                switch (card.getEra()) {
                    case I -> era_I_cards.add(card);
                    case II -> era_II_cards.add(card);
                    case III -> era_III_cards.add(card);
                }
            }

            //3. Shuffle each era's list to ensure randomness
            Collections.shuffle(era_I_cards);
            Collections.shuffle(era_II_cards);
            Collections.shuffle(era_III_cards);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public BuildingCard draw() { return null; }

    // TODO: capire come eventualmente ottimizzarlo, passando magari l'era (visto che in un determinato momento, si possono prendere solo edifici dell'era in cui si è) e cercando solo in quella lista, invece che in tutte e tre
    public BuildingCard draw(int id) {
        for (BuildingCard card : era_I_cards) {
            if (card.getId() == id) {
                era_I_cards.remove(card);
                return card;
            }
        }
        for (BuildingCard card : era_II_cards) {
            if (card.getId() == id) {
                era_II_cards.remove(card);
                return card;
            }
        }
        for (BuildingCard card : era_III_cards) {
            if (card.getId() == id) {
                era_III_cards.remove(card);
                return card;
            }
        }
        throw new NoSuchElementException("No card with ID " + id + " found in the deck.");
    }

    public void showEraBuildings(Era era) {
        switch (era) {
            case I -> era_I_cards.stream().forEach(System.out::println);
            case II -> era_II_cards.stream().forEach(System.out::println);
            case III -> era_III_cards.stream().forEach(System.out::println);
            default -> throw new IllegalArgumentException("Invalid era: " + era);
        }
    }
}