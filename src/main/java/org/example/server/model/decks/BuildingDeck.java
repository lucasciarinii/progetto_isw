package org.example.server.model.decks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.model.board.Board;
import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.enums.Era;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deck of building cards, loaded from JSON and arranged by era.
 */
public class BuildingDeck extends Deck<BuildingCard> {

    /**
     * Builds the deck and places era I cards on the board's top row.
     *
     * @param numPlayers number of players
     * @param b target board
     */
    public BuildingDeck(int numPlayers, Board b) {
        super();
        initializeDeck(numPlayers);
        addCardToTopRow(b, Era.I); // add era I cards to the top row of the board
    }

    /**
     * Initializes and trims the deck based on player count.
     *
     * @param numPlayers number of players
     */
    private void initializeDeck(int numPlayers) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. Read all objects from the JSON file into a List<Card>
            //Path p = Path.of("src/main/java/org/example/server/model/decks/decks_json/buildingCards.json");
            Path p = Path.of(getClass().getClassLoader().getResource("json/buildingCards.json").toURI());
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
                    era_I_cards = new ArrayList<>(era_I_cards.subList(0, 1));
                    era_II_cards = new ArrayList<>(era_II_cards.subList(0, 2));
                    era_III_cards = new ArrayList<>(era_III_cards.subList(0, 3));
                }
                case 3 -> {
                    era_I_cards = new ArrayList<>(era_I_cards.subList(0, 2));
                    era_II_cards = new ArrayList<>(era_II_cards.subList(0, 2));
                    era_III_cards = new ArrayList<>(era_III_cards.subList(0, 4));
                }
                case 4 -> {
                    era_I_cards = new ArrayList<>(era_I_cards.subList(0, 2));
                    era_II_cards = new ArrayList<>(era_II_cards.subList(0, 3));
                    era_III_cards = new ArrayList<>(era_III_cards.subList(0, 4));
                }
                case 5 -> {
                    era_I_cards = new ArrayList<>(era_I_cards.subList(0, 2));
                    era_II_cards = new ArrayList<>(era_II_cards.subList(0, 3));
                    era_III_cards = new ArrayList<>(era_III_cards.subList(0, 5));
                }
                default -> throw new IllegalArgumentException("Invalid number of players: " + numPlayers);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves all cards of the given era to the board's top row.
     *
     * @param b board to receive the cards
     * @param era era to move
     */
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