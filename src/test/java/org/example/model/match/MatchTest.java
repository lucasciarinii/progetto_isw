package org.example.model.match;

import org.example.model.board.PlayerSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

	private List<Player> createPlayers(int count) {
		List<Player> players = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			players.add(new Player("Player" + i));
		}
		return players;
	}

	private static Stream<Arguments> playerCountsAndExpectedFood() {
		return Stream.of(
				Arguments.of(2, List.of(2, 3)),
				Arguments.of(3, List.of(2, 3, 3)),
				Arguments.of(4, List.of(2, 3, 3, 4)),
				Arguments.of(5, List.of(2, 3, 3, 4, 4))
		);
	}

	// Test that the constructor rejects a null list.
	@Test
	void constructor_nullPlayers_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new Match(null));
	}

	// Test that the constructor initializes board and gameState for 2..5 players.
	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4, 5})
	void constructor_createsBoardAndGameState(int playerCount) {
		List<Player> players = createPlayers(playerCount);

		Match match = new Match(players);

		assertNotNull(match.getBoard());
		assertNotNull(match.getGameState());
		assertEquals(playerCount, match.getPlayers().size());
		assertTrue(match.getPlayers().containsAll(players));
	}

	// Test that the constructor makes a defensive copy of the input player list.
	@Test
	void constructor_copiesInputPlayersList() {
		List<Player> players = createPlayers(2);
		List<Player> expectedPlayers = new ArrayList<>(players);

		Match match = new Match(players);

		players.add(new Player("Player2"));

		assertEquals(2, match.getPlayers().size());
		assertTrue(match.getPlayers().containsAll(expectedPlayers));
		assertFalse(match.getPlayers().contains(players.get(2)));
	}

	// Test that getPlayers returns an unmodifiable list.
	@Test
	void getPlayers_returnsUnmodifiableList() {
		Match match = new Match(createPlayers(2));

		assertThrows(UnsupportedOperationException.class,
				() -> match.getPlayers().add(new Player("Player2")));
	}

	// Test the initial food assignment for all supported player counts.
	@ParameterizedTest
	@MethodSource("playerCountsAndExpectedFood")
	void init_assignsInitialFoodForAllSupportedPlayerCounts(int playerCount, List<Integer> expectedFood) {
		Match match = new Match(createPlayers(playerCount));

		List<Integer> foods = new ArrayList<>();
		for (Player player : match.getPlayers()) {
			foods.add(player.getFood());
		}
		foods.sort(Integer::compareTo);

		assertEquals(expectedFood, foods);
	}

	// Test that the turn order contains every player exactly once.
	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4, 5})
	void init_createsTurnOrderForAllPlayers(int playerCount) {
		Match match = new Match(createPlayers(playerCount));

		List<PlayerSlot> slots = match.getBoard().getTurnOrderTile().getSlots();
		Set<Player> uniquePlayers = new HashSet<>();

		assertEquals(match.getPlayers().size(), slots.size());

		for (PlayerSlot slot : slots) {
			assertNotNull(slot.getPlayer());
			uniquePlayers.add(slot.getPlayer());
		}

		assertEquals(slots.size(), uniquePlayers.size());
		assertTrue(uniquePlayers.containsAll(match.getPlayers()));
	}

	// Test that the board rows are initialized with cards.
	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4, 5})
	void init_boardRowsAreInitialized(int playerCount) {
		Match match = new Match(createPlayers(playerCount));

		assertFalse(match.getBoard().getTopRow().isEmpty());
		assertFalse(match.getBoard().getBottomRow().isEmpty());
	}






}