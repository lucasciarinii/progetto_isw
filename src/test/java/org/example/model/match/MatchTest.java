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

    // Test that placeTotemOnOfferTile puts the player on the selected offer tile.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_placesPlayerOnSelectedOfferTile(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();

        match.placeTotemOnOfferTile(player, 1);

        assertSame(player, match.getBoard().getOfferTrack().get(0).getPlayer());
    }

    // Test that placeTotemOnOfferTile removes the player from turn order slots.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_removesPlayerFromTurnOrderTile(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();

        match.placeTotemOnOfferTile(player, 1);

        for (PlayerSlot slot : match.getBoard().getTurnOrderTile().getSlots()) {
            assertNotEquals(player, slot.getPlayer());
        }
    }

    // Test that only the selected player is removed from turn order slots.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_onlyRemovesSelectedPlayer(int playerCount) {
        Match match = new Match(createPlayers(playerCount));

        List<Player> playersBefore = new ArrayList<>();
        for (PlayerSlot slot : match.getBoard().getTurnOrderTile().getSlots()) {
            playersBefore.add(slot.getPlayer());
        }

        Player selectedPlayer = playersBefore.get(0);
        Set<Player> expectedRemainingPlayers = new HashSet<>(playersBefore);
        expectedRemainingPlayers.remove(selectedPlayer);

        match.placeTotemOnOfferTile(selectedPlayer, 1);

        Set<Player> remainingPlayers = new HashSet<>();
        for (PlayerSlot slot : match.getBoard().getTurnOrderTile().getSlots()) {
            if (slot.getPlayer() != null) {
                remainingPlayers.add(slot.getPlayer());
            }
        }

        assertFalse(remainingPlayers.contains(selectedPlayer));
        assertEquals(expectedRemainingPlayers, remainingPlayers);
    }

    // Test that tile index is interpreted as one-based (tile 1 -> first offer tile).
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_usesOneBasedTileIndex(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player selectedPlayer = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();

        match.placeTotemOnOfferTile(selectedPlayer, 1);

        assertSame(selectedPlayer, match.getBoard().getOfferTrack().get(0).getPlayer());
    }

    // Test that tile index 0 throws an out-of-bounds exception.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_invalidLowIndex_throws(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();

        assertThrows(IndexOutOfBoundsException.class,
                () -> match.placeTotemOnOfferTile(player, 0));
    }

    // Test that an index greater than offerTrack size throws an out-of-bounds exception.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_invalidHighIndex_throws(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int invalidTileIndex = match.getBoard().getOfferTrack().size() + 1;

        assertThrows(IndexOutOfBoundsException.class,
                () -> match.placeTotemOnOfferTile(player, invalidTileIndex));
    }

    // Test current behavior: an external player is still placed on offer track and turn order stays unchanged.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void placeTotemOnOfferTile_playerNotInTurnOrderStillPlacedOnOfferTrack(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        List<Player> turnOrderBefore = new ArrayList<>();
        for (PlayerSlot slot : match.getBoard().getTurnOrderTile().getSlots()) {
            turnOrderBefore.add(slot.getPlayer());
        }

        Player externalPlayer = new Player("ExternalPlayer");

        match.placeTotemOnOfferTile(externalPlayer, 1);

        assertSame(externalPlayer, match.getBoard().getOfferTrack().get(0).getPlayer());

        List<Player> turnOrderAfter = new ArrayList<>();
        for (PlayerSlot slot : match.getBoard().getTurnOrderTile().getSlots()) {
            turnOrderAfter.add(slot.getPlayer());
        }

        assertEquals(turnOrderBefore, turnOrderAfter);
    }




}