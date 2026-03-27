package org.example.model.decks;

import org.example.model.board.Board;
import org.example.model.enums.Era;
import org.example.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildingDeckTest {

	@Test
	@Tag("BuildingDeck")
	@DisplayName("Stampa gli ID delle carte nelle 3 liste del BuildingDeck")
	void printIdsFromThreeLists() {
        // 2 Players Case
        List<Player> players = List.of(new Player("Alice"), new Player("Bob"));
		Board b = new Board(players);

        b.getBuildingDeck().era_I_cards.forEach(card -> System.out.println("Era I card ID: " + card.getId()));
        b.getBuildingDeck().era_II_cards.forEach(card -> System.out.println("Era II card ID: " + card.getId()));
        b.getBuildingDeck().era_III_cards.forEach(card -> System.out.println("Era III card ID: " + card.getId()));

        // after initialization we already have the Era I card in the top row, so in the top row we should have
        assertEquals(5, b.getBuildingDeck().era_I_cards.size() + b.getBuildingDeck().era_II_cards.size() + b.getBuildingDeck().era_III_cards.size());
        assertEquals(7, b.getTopRow().size());
    }
}