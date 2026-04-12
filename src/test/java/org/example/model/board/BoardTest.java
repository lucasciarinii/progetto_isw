package org.example.model.board;

import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.eventCards.EventCard;
import org.example.model.enums.Era;
import org.example.model.enums.OfferEffect;
import org.example.model.match.Player;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    // Verifies that the constructor rejects a null players list.
    @Test
    void constructor_nullPlayers_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Board(null));
    }

    // Verifies that out-of-range player counts (0, 1, 6) throw an exception.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 6})
    void constructor_invalidPlayerCount_throwsIllegalArgumentException(int numPlayers) {
        List<Player> players = createPlayers(numPlayers);
        assertThrows(IllegalArgumentException.class, () -> new Board(players));
    }

    // Verifies that the offer track is initialized with the correct effect sequence for 2-5 players.
    @ParameterizedTest
    @MethodSource("offerTrackByPlayerCount")
    void constructor_validPlayerCount_initializesOfferTrack(
            int numPlayers,
            List<OfferEffect> expectedEffects
    ) {
        Board board = new Board(createPlayers(numPlayers));

        List<OfferEffect> actualEffects = board.getOfferTrack()
                .stream()
                .map(OfferTile::getOfferEffect)
                .collect(Collectors.toList());

        assertEquals(expectedEffects, actualEffects);
    }

    // Verifies that turn-order slots have the expected players, food bonuses, and points for each valid setup.
    @ParameterizedTest
    @MethodSource("turnOrderEffectsByPlayerCount")
    void constructor_validPlayerCount_initializesTurnOrderTileSlots(
            int numPlayers,
            int[] expectedFood,
            int[] expectedPoints
    ) {
        List<Player> players = createPlayers(numPlayers);
        Board board = new Board(players);

        List<PlayerSlot> slots = board.getTurnOrderTile().getSlots();

        assertEquals(numPlayers, slots.size());
        for (int i = 0; i < numPlayers; i++) {
            assertSame(players.get(i), slots.get(i).getPlayer());
            assertEquals(expectedFood[i], slots.get(i).getFood());
            assertEquals(expectedPoints[i], slots.get(i).getPoints());
        }
    }

    // Verifies top-row and bottom-row initialization invariants, including sizes and card types.
    @ParameterizedTest
    @MethodSource("rowsByPlayerCount")
    void constructor_validPlayerCount_initializesRowsWithExpectedInvariants(
            int numPlayers,
            int expectedEraIBuildingsOnTopRow
    ) {
        Board board = new Board(createPlayers(numPlayers));

        List<Card> topRow = board.getTopRow();
        List<Card> bottomRow = board.getBottomRow();

        assertEquals(numPlayers + 1, bottomRow.size());
        assertTrue(bottomRow.stream().allMatch(card -> !(card instanceof EventCard)));

        long buildingCardsInTopRow = topRow.stream().filter(card -> card instanceof BuildingCard).count();
        assertEquals(expectedEraIBuildingsOnTopRow, buildingCardsInTopRow);

        long nonBuildingCardsInTopRow = topRow.size() - buildingCardsInTopRow;
        assertEquals(numPlayers + 4L, nonBuildingCardsInTopRow);

        assertEquals(numPlayers + 4 + expectedEraIBuildingsOnTopRow, topRow.size());
        assertTrue(topRow.stream().allMatch(card -> card.getEra() == Era.I));

        assertTrue(topRow.stream().allMatch(card -> card != null));
        assertTrue(bottomRow.stream().allMatch(card -> card != null));
    }

    // Verifies that the offer track exposed by the board is immutable.
    @Test
    void constructor_offerTrackIsImmutable() {
        Board board = new Board(createPlayers(2));

        assertThrows(UnsupportedOperationException.class,
                () -> board.getOfferTrack().add(new OfferTile(OfferEffect.D)));
    }

    // Edge case: verifies that a list containing a null player is rejected.
    @Test
    void constructor_playersListContainingNull_throwsNullPointerException() {
        List<Player> playersWithNull = new ArrayList<>();
        playersWithNull.add(new Player("Alice"));
        playersWithNull.add(null);

        assertThrows(NullPointerException.class, () -> new Board(playersWithNull));
    }

    private static Stream<Arguments> offerTrackByPlayerCount() {
        return Stream.of(
                Arguments.of(2, List.of(OfferEffect.D, OfferEffect.U, OfferEffect.DU, OfferEffect.UU)),
                Arguments.of(3, List.of(OfferEffect.D, OfferEffect.U, OfferEffect.DD, OfferEffect.DU, OfferEffect.UU)),
                Arguments.of(4, List.of(OfferEffect.D, OfferEffect.U, OfferEffect.DD, OfferEffect.DU, OfferEffect.UU, OfferEffect.DUU)),
                Arguments.of(5, List.of(OfferEffect.FOOD, OfferEffect.D, OfferEffect.U, OfferEffect.DD, OfferEffect.DU, OfferEffect.UU, OfferEffect.DUU))
        );
    }

    private static Stream<Arguments> turnOrderEffectsByPlayerCount() {
        return Stream.of(
                Arguments.of(2, new int[]{1, -1}, new int[]{0, -2}),
                Arguments.of(3, new int[]{2, 0, -1}, new int[]{0, 0, -2}),
                Arguments.of(4, new int[]{2, 1, 0, -1}, new int[]{0, 0, 0, -2}),
                Arguments.of(5, new int[]{3, 1, 0, 0, -1}, new int[]{0, 0, 0, 0, -2})
        );
    }

    private static Stream<Arguments> rowsByPlayerCount() {
        return Stream.of(
                Arguments.of(2, 1),
                Arguments.of(3, 2),
                Arguments.of(4, 2),
                Arguments.of(5, 2)
        );
    }

    private static List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player("p" + i));
        }
        return players;
    }
}
