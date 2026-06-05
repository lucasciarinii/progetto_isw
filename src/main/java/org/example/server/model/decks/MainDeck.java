package org.example.server.model.decks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.model.cards.Card;
import org.example.server.model.cards.characters.Character;
import org.example.server.model.cards.eventCards.EventCard;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Deck containing character and event cards grouped by era.
 */
public class MainDeck extends Deck<Card> {

    /**
     * Builds and initializes the main deck based on player count.
     *
     * @param numPlayers number of players (2-5)
     */
    public MainDeck(int numPlayers) {
        super();
        initializeDeck(numPlayers);
    }

    /**
     * Draws the next available card, ordered by eras.
     *
     * @return the next card in order of eras
     * @throws NoSuchElementException when all eras are empty
     */
    public Card draw() {
        if (!era_I_cards.isEmpty()) {
            return era_I_cards.removeFirst();
        }
        if (!era_II_cards.isEmpty()) {
            return era_II_cards.removeFirst();
        }
        if (!era_III_cards.isEmpty()) {
            return era_III_cards.removeFirst();
        }

        throw new NoSuchElementException("No cards left in deck");
    }

    /**
     * Initializes the deck by loading cards from JSON and shuffling them.
     *
     * @param numPlayers number of players (2-5)
     */
    private void initializeDeck(int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            throw new IllegalArgumentException("Number of players must be between 2 and 5");
        }

        ObjectMapper mapper = new ObjectMapper();
        loadCardsFromJson(mapper, numPlayers);
    }


    /**
     * Loads character and event cards from JSON resources and splits them by era.
     *
     * @param mapper object mapper used to parse JSON
     * @param numPlayers number of players (2-5)
     */
    private void loadCardsFromJson(ObjectMapper mapper, int numPlayers) {

        String charactersPath = "json/characters_%dp.json".formatted(numPlayers);
        String eventsPath = "json/events.json";

        try (
                InputStream characterStream = getClass().getClassLoader().getResourceAsStream(charactersPath);
                InputStream eventStream = getClass().getClassLoader().getResourceAsStream(eventsPath)
        ) {
            if (characterStream == null) {
                throw new IllegalStateException("Missing resource: " + charactersPath);
            }
            if (eventStream == null) {
                throw new IllegalStateException("Missing resource: " + eventsPath);
            }

            // 1. Read all character objects from the JSON file into a List<Character>
            List<Character> characterCards = mapper.readValue(
                    characterStream,
                    new TypeReference<>() {
                    }
            );

            // 2. Filter on specific eras and add to the respective lists
            for (Character card : characterCards) {
                switch (card.getEra()) {
                    case I -> era_I_cards.add(card);
                    case II -> era_II_cards.add(card);
                    case III -> era_III_cards.add(card);
                }
            }

            // 1. Read all event objects from the JSON file into a List<EventCard>>
            List<EventCard> eventCards = mapper.readValue(
                    eventStream,
                    new TypeReference<>() {
                    }
            );

            // 2. Filter on specific eras and add to the respective lists
            for (EventCard card : eventCards) {
                switch (card.getEra()) {
                    case I -> era_I_cards.add(card);
                    case II -> era_II_cards.add(card);
                    case III -> era_III_cards.add(card);
                }
            }

            //3. Shuffle each era's list to ensure randomness
            Collections.shuffle(era_I_cards);
            Collections.shuffle(era_II_cards);

            List<Card> eraIIINonFinal = new ArrayList<>();
            List<Card> eraIIIFinalEvents = new ArrayList<>();
            for (Card card : era_III_cards) {
                if(card.isEventCard() && ((EventCard) card).isEraFinal()) {
                    eraIIIFinalEvents.add(card);
                } else {
                    eraIIINonFinal.add(card);
                }
            }

            Collections.shuffle(eraIIINonFinal);
            Collections.shuffle(eraIIIFinalEvents);

            era_III_cards.clear();
            era_III_cards.addAll(eraIIINonFinal);
            era_III_cards.addAll(eraIIIFinalEvents);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load main deck resources", e);
        }
    }
}