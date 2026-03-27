package org.example.model.decks;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.model.cards.buildingCards.BuildingCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.enums.Era;
import org.example.model.board.Board;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class BuildingDeck extends Deck<BuildingCard> {

    public BuildingDeck(int numPlayers, Board b) {
        super();
        initializeDeck(numPlayers);
        addCardToTopRow(b, Era.I); // add era I cards to the top row of the board
    }

    public BuildingCard draw(BuildingCard buildingCard) {
        return buildingCard;
    }

    private void initializeDeck(int numPlayers) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. Read all objects from the JSON file into a List<Card>
            Path p = Path.of("src/main/java/org/example/model/decks/decks_json/buildingCards.json");
            List<BuildingCard> allCards = mapper.readValue(
                    p.toFile(),
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

            //4. Keep only the number of cards needed for the game based on the number of players
            switch (numPlayers) {
                case 2 -> {
                    era_I_cards = era_I_cards.subList(0, 1);
                    era_II_cards = era_II_cards.subList(0, 2);
                    era_III_cards = era_III_cards.subList(0, 3);
                }
                case 3 -> {
                    era_I_cards = era_I_cards.subList(0, 2);
                    era_II_cards = era_II_cards.subList(0, 2);
                    era_III_cards = era_III_cards.subList(0, 4);
                }
                case 4 -> {
                    era_I_cards = era_I_cards.subList(0, 2);
                    era_II_cards = era_II_cards.subList(0, 3);
                    era_III_cards = era_III_cards.subList(0, 4);
                }
                case 5 -> {
                    era_I_cards = era_I_cards.subList(0, 2);
                    era_II_cards = era_II_cards.subList(0, 3);
                    era_III_cards = era_III_cards.subList(0, 5);
                }
                default -> throw new IllegalArgumentException("Invalid number of players: " + numPlayers);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCardToTopRow(Board b, Era era) {
        switch (era) {
            case I -> {
                b.getTopRow().addAll(era_I_cards);
                era_I_cards.clear();
            }
            case II -> {
                b.getTopRow().addAll(era_II_cards);
                era_II_cards.clear();
            }
            case III -> {
                b.getTopRow().addAll(era_III_cards);
                era_III_cards.clear();
            }
        }
    }

}