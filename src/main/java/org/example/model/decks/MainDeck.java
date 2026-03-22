package org.example.model.decks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.cards.Card;
import org.example.model.cards.characters.Character;
import org.example.model.cards.eventCards.EventCard;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class MainDeck extends Deck<Card>{

    public MainDeck(int numPlayers) {
        super();
        initializeDeck();
    }

    public Card draw(){
        if (!era_I_cards.isEmpty()) {
            return era_I_cards.remove(0);
        }
        if (!era_II_cards.isEmpty()) {
            return era_II_cards.remove(0);
        }
        if (!era_III_cards.isEmpty()) {
            return era_III_cards.remove(0);
        }

        throw new NoSuchElementException("No cards left in deck");
    }

    private void initializeDeck() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. Read all character objects from the JSON file into a List<Character>>
            Path p = Path.of("src/main/java/org/example/model/decks/decks_json/characters_2p.json");
            List<Character> characterCards = mapper.readValue(
                    p.toFile(),
                    new TypeReference<List<Character>>(){}
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
            p = Path.of("src/main/java/org/example/model/decks/decks_json/events.json");
            List<EventCard> eventCards = mapper.readValue(
                    p.toFile(),
                    new TypeReference<List<EventCard>>(){}
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
            Collections.shuffle(era_III_cards);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}