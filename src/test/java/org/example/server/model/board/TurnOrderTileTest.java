package org.example.server.model.board;

import org.example.server.model.board.PlayerSlot;
import org.example.server.model.board.TurnOrderTile;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderTileTest {

	// Verifies that constructor initializes correct number of slots with proper player assignments.
	@ParameterizedTest(name = "Test with {0} players")
	@MethodSource("providePlayerListsWithExpectedSlotValues")
	void constructor_withValidPlayerCount_initializesCorrectSlots(int playerCount, List<Player> players,
	                                                               int[] expectedFood, int[] expectedPoints) {
		TurnOrderTile tile = new TurnOrderTile(players);

		// Verify correct number of slots created
		assertEquals(playerCount, tile.getSlots().size());

		// Verify each slot has the correct player and food/points values
		for (int i = 0; i < playerCount; i++) {
			assertSame(players.get(i), tile.getSlots().get(i).getPlayer());
			assertEquals(expectedFood[i], tile.getSlots().get(i).getFood());
			assertEquals(expectedPoints[i], tile.getSlots().get(i).getPoints());
		}
	}

	// Verifies that null player list is rejected at construction time.
	@Test
	void constructor_withNullPlayerList_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new TurnOrderTile(null));
	}

	// Verifies that empty player list is rejected (unsupported player count).
	@Test
	void constructor_withEmptyPlayerList_throwsIllegalArgumentException() {
		List<Player> emptyList = new ArrayList<>();
		assertThrows(IllegalArgumentException.class, () -> new TurnOrderTile(emptyList));
	}

	// Verifies that unsupported player counts (1, 6, etc.) are rejected.
	@ParameterizedTest(name = "Test with {0} players (unsupported)")
	@ValueSource(ints = {1, 6, 7, 10})
	void constructor_withUnsupportedPlayerCount_throwsIllegalArgumentException(int count) {
		List<Player> players = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			players.add(new Player("player" + i));
		}
		assertThrows(IllegalArgumentException.class, () -> new TurnOrderTile(players));
	}

	// Provides test data: player count, player list, expected food array, expected points array
	private static Stream<org.junit.jupiter.params.provider.Arguments> providePlayerListsWithExpectedSlotValues() {
		return Stream.of(
			// 2 players: [+1, 0] food, [0, -2] points
			org.junit.jupiter.params.provider.Arguments.of(
				2,
				List.of(new Player("alice"), new Player("bob")),
				new int[]{1, -1},
				new int[]{0, -2}
			),
			// 3 players: [+2, 0, -1] food, [0, 0, -2] points
			org.junit.jupiter.params.provider.Arguments.of(
				3,
				List.of(new Player("alice"), new Player("bob"), new Player("charlie")),
				new int[]{2, 0, -1},
				new int[]{0, 0, -2}
			),
			// 4 players: [+2, +1, 0, -1] food, [0, 0, 0, -2] points
			org.junit.jupiter.params.provider.Arguments.of(
				4,
				List.of(new Player("alice"), new Player("bob"), new Player("charlie"), new Player("dave")),
				new int[]{2, 1, 0, -1},
				new int[]{0, 0, 0, -2}
			),
			// 5 players: [+3, +1, 0, 0, -1] food, [0, 0, 0, 0, -2] points
			org.junit.jupiter.params.provider.Arguments.of(
				5,
				List.of(new Player("alice"), new Player("bob"), new Player("charlie"), new Player("dave"), new Player("eve")),
				new int[]{3, 1, 0, 0, -1},
				new int[]{0, 0, 0, 0, -2}
			)
		);
	}

	// Verifies that getSlots returns the internal list of slots.
	@Test
	void getSlots_returnsPlayerSlotList() {
		List<Player> players = List.of(new Player("p1"), new Player("p2"));
		TurnOrderTile tile = new TurnOrderTile(players);

		List<PlayerSlot> slots = tile.getSlots();
		assertNotNull(slots);
		assertEquals(2, slots.size());
	}

}