package org.example.model.match;

import org.example.model.board.PlayerSlot;
import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.buildingCards.SetCollectionFoodBC;
import org.example.model.cards.characters.Character;
import org.example.model.cards.characters.Gatherer;
import org.example.model.cards.characters.Hunter;
import org.example.model.cards.eventCards.*;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.enums.OfferEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.NoSuchElementException;
import java.lang.reflect.Field;
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

    private static Stream<Arguments> blankStringOfferEffects() {
        return Stream.of(
                Arguments.of(5, OfferEffect.FOOD, null),
                Arguments.of(2, OfferEffect.D, "Invalid String: player must select only 1 card"),
                Arguments.of(2, OfferEffect.U, "Invalid String: player must select only 1 card"),
                Arguments.of(3, OfferEffect.DD, "Invalid String: player must select exactly 2 IDs from cards"),
                Arguments.of(4, OfferEffect.DU, "Invalid String: player must select exactly 2 IDs from cards"),
                Arguments.of(2, OfferEffect.UU, "Invalid String: player must select exactly 2 IDs from cards"),
                Arguments.of(4, OfferEffect.DUU, "Invalid String: player must select exactly 3 IDs from cards")
        );
    }

    private static int findOfferTileIndex(Match match, OfferEffect effect) {
        for (int i = 0; i < match.getBoard().getOfferTrack().size(); i++) {
            if (match.getBoard().getOfferTrack().get(i).getOfferEffect() == effect) {
                return i + 1;
            }
        }

        throw new IllegalStateException("Offer effect not found on offer track: " + effect);
    }

    private static int totalOwnedCharacters(Player player) {
        return player.getInventors().size()
                + player.getGatherers().size()
                + player.getShamans().size()
                + player.getBuilders().size()
                + player.getArtists().size()
                + player.getHunters().size();
    }

    private static int nextUnusedCardId(Match match) {
        int maxTop = match.getBoard().getTopRow().stream().mapToInt(Card::getId).max().orElse(0);
        int maxBottom = match.getBoard().getBottomRow().stream().mapToInt(Card::getId).max().orElse(0);
        return Math.max(maxTop, maxBottom) + 100;
    }

    private static void setMainDeckCards(Match match, List<Card> eraICards, List<Card> eraIICards, List<Card> eraIIICards) {
        try {
            Field eraIField = org.example.model.decks.Deck.class.getDeclaredField("era_I_cards");
            Field eraIIField = org.example.model.decks.Deck.class.getDeclaredField("era_II_cards");
            Field eraIIIField = org.example.model.decks.Deck.class.getDeclaredField("era_III_cards");

            eraIField.setAccessible(true);
            eraIIField.setAccessible(true);
            eraIIIField.setAccessible(true);

            eraIField.set(match.getBoard().getMainDeck(), new ArrayList<>(eraICards));
            eraIIField.set(match.getBoard().getMainDeck(), new ArrayList<>(eraIICards));
            eraIIIField.set(match.getBoard().getMainDeck(), new ArrayList<>(eraIIICards));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Gatherer gatherer(int id, Era era) {
        return new Gatherer(id, era, CharacterType.GATHERER);
    }

    private static Hunter hunter(int id) {
        return new Hunter(id, Era.I, CharacterType.HUNTER, false);
    }

    private static HuntEvent huntEvent(int id, int points) {
        return new HuntEvent(id, Era.I, false, EventEffect.HUNT_EVENT, points);
    }

    private static Sustenance sustenance(int id, int points) {
        return new Sustenance(id, Era.I, false, EventEffect.SUSTENANCE, points);
    }

    //! ===================================
    //! INIT & CONSTRUCTOR TESTS
    //! ===================================

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

    //! ===================================
    //! placeTotemOnOfferTile
    //! ===================================

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

    //! ===================================
    //! 3) offerTileAction(...) - TRASVERSAL TESTS
    //! ===================================

    // Test that offerTileAction throws when the player is not on any offer tile.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void offerTileAction_playerWithoutOfferTile_throwsNullPointerException(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getPlayers().get(0);

        assertThrows(NullPointerException.class,
                () -> match.offerTileAction(player, "1"));
    }

    // Test that invalid ID strings are rejected by offerTileAction.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void offerTileAction_invalidIdString_throwsIllegalArgumentException(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getPlayers().get(0);

        for (String invalidInput : List.of("abc", "1,x", "1,,2")) {
            assertThrows(IllegalArgumentException.class,
                    () -> match.offerTileAction(player, invalidInput));
        }
    }

    // Test that duplicate IDs are rejected by offerTileAction.
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void offerTileAction_duplicateIds_throwsIllegalArgumentException(int playerCount) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getPlayers().get(0);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "5,5"));
    }

    // Test that offerTileAction accepts trimmed spaces and still reads the selected IDs correctly.
    @Test
    void offerTileAction_trimmedSpacesAreAccepted() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int offerTileIndex = findOfferTileIndex(match, OfferEffect.DU);

        match.placeTotemOnOfferTile(player, offerTileIndex);

        Character bottomCharacter = match.getBoard().getBottomRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        Character topCharacter = match.getBoard().getTopRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();

        String input = "  " + bottomCharacter.getId() + " , " + topCharacter.getId() + "  ";

        assertDoesNotThrow(() -> match.offerTileAction(player, input));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == bottomCharacter.getId()));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == topCharacter.getId()));
    }

    // Test that blank input is accepted only for FOOD and rejected for all card-selection offers.
    @ParameterizedTest
    @MethodSource("blankStringOfferEffects")
    void offerTileAction_blankStringWorksOnlyForFood(int playerCount, OfferEffect effect, String expectedMessage) {
        Match match = new Match(createPlayers(playerCount));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int offerTileIndex = findOfferTileIndex(match, effect);
        int initialFood = player.getFood();

        match.placeTotemOnOfferTile(player, offerTileIndex);

        if (effect == OfferEffect.FOOD) {
            assertDoesNotThrow(() -> match.offerTileAction(player, ""));
            assertEquals(initialFood + 3, player.getFood());
        } else {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> match.offerTileAction(player, ""));
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    //! ===================================
    //! 3) offerTileAction(...) - FOOD
    //! ===================================

    // Test that FOOD offer action adds exactly 3 food without requiring card IDs.
    @Test
    void offerTileAction_food_addsThreeFood() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int offerTileIndex = findOfferTileIndex(match, OfferEffect.FOOD);
        int initialFood = player.getFood();

        match.placeTotemOnOfferTile(player, offerTileIndex);

        assertDoesNotThrow(() -> match.offerTileAction(player, ""));
        assertEquals(initialFood + 3, player.getFood());
    }

    //! ===================================
    //! 4) offerTileAction(...) - D
    //! ===================================

    // Test that D selects one Character from bottomRow and moves it to the player.
    @Test
    void offerTileAction_D_selectsOneCharacterFromBottomRow() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int dTileIndex = findOfferTileIndex(match, OfferEffect.D);

        match.placeTotemOnOfferTile(player, dTileIndex);

        Character selected = match.getBoard().getBottomRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, String.valueOf(selected.getId())));
        assertEquals(ownedBefore + 1, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == selected.getId()));
    }

    // Test that D rejects inputs containing more than one card ID.
    @Test
    void offerTileAction_D_rejectsMoreThanOneId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int dTileIndex = findOfferTileIndex(match, OfferEffect.D);

        match.placeTotemOnOfferTile(player, dTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2"));
    }

    // Test that D rejects an ID pointing to a BuildingCard in bottomRow.
    @Test
    void offerTileAction_D_rejectsBuildingCardId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int dTileIndex = findOfferTileIndex(match, OfferEffect.D);
        int buildingId = nextUnusedCardId(match);

        match.placeTotemOnOfferTile(player, dTileIndex);
        match.getBoard().getBottomRow().add(new SetCollectionFoodBC(
                buildingId,
                Era.I,
                1,
                1,
                BuildingCardType.SetCollectionFoodBC,
                false
        ));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(buildingId)));
    }

    // Test that D rejects an ID pointing to an EventCard in bottomRow.
    @Test
    void offerTileAction_D_rejectsEventCardId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int dTileIndex = findOfferTileIndex(match, OfferEffect.D);
        int eventId = nextUnusedCardId(match);

        match.placeTotemOnOfferTile(player, dTileIndex);
        match.getBoard().getBottomRow().add(new Sustenance(
                eventId,
                Era.I,
                false,
                EventEffect.SUSTENANCE,
                1
        ));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(eventId)));
    }

    // Test that D rejects an ID that does not match a valid Character in bottomRow.
    @Test
    void offerTileAction_D_rejectsMissingId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int dTileIndex = findOfferTileIndex(match, OfferEffect.D);
        int missingId = nextUnusedCardId(match);

        match.placeTotemOnOfferTile(player, dTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(missingId)));
    }

    //! ===================================
    //! 5) offerTileAction(...) - U
    //! ===================================

    // Test that U selects one Character from topRow and moves it to the player.
    @Test
    void offerTileAction_U_selectsOneCharacterFromTopRow() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uTileIndex = findOfferTileIndex(match, OfferEffect.U);

        match.placeTotemOnOfferTile(player, uTileIndex);

        Character selected = match.getBoard().getTopRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, String.valueOf(selected.getId())));
        assertEquals(ownedBefore + 1, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == selected.getId()));
    }

    // Test that U rejects inputs with zero IDs or more than one ID.
    @Test
    void offerTileAction_U_rejectsWrongNumberOfIds() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uTileIndex = findOfferTileIndex(match, OfferEffect.U);

        match.placeTotemOnOfferTile(player, uTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, ""));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2"));
    }

    // Test that U rejects Building/Event IDs because topRow selection only accepts Character cards.
    @Test
    void offerTileAction_U_rejectsNonCharacterCard() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uTileIndex = findOfferTileIndex(match, OfferEffect.U);
        int buildingId = nextUnusedCardId(match);
        int eventId = buildingId + 1;

        match.placeTotemOnOfferTile(player, uTileIndex);
        match.getBoard().getTopRow().add(new SetCollectionFoodBC(
                buildingId,
                Era.I,
                1,
                1,
                BuildingCardType.SetCollectionFoodBC,
                false
        ));
        match.getBoard().getTopRow().add(new Sustenance(
                eventId,
                Era.I,
                false,
                EventEffect.SUSTENANCE,
                1
        ));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(buildingId)));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(eventId)));
    }

    // Test that U rejects an ID not matching any valid Character in topRow.
    @Test
    void offerTileAction_U_rejectsMissingId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uTileIndex = findOfferTileIndex(match, OfferEffect.U);
        int missingId = nextUnusedCardId(match);

        match.placeTotemOnOfferTile(player, uTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, String.valueOf(missingId)));
    }

    //! ===================================
    //! 6) offerTileAction(...) - DD
    //! ===================================

    // Test that DD selects exactly two Characters from bottomRow and assigns both to the player.
    @Test
    void offerTileAction_DD_selectsTwoCharactersFromBottomRow() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int ddTileIndex = findOfferTileIndex(match, OfferEffect.DD);
        int firstId = nextUnusedCardId(match);
        int secondId = firstId + 1;

        match.placeTotemOnOfferTile(player, ddTileIndex);
        match.getBoard().getBottomRow().add(new Gatherer(firstId, Era.I, CharacterType.GATHERER));
        match.getBoard().getBottomRow().add(new Gatherer(secondId, Era.I, CharacterType.GATHERER));
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, firstId + "," + secondId));
        assertEquals(ownedBefore + 2, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == firstId));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == secondId));
    }

    // Test that DD rejects input with only one ID.
    @Test
    void offerTileAction_DD_rejectsOnlyOneId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int ddTileIndex = findOfferTileIndex(match, OfferEffect.DD);

        match.placeTotemOnOfferTile(player, ddTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1"));
    }

    // Test that DD rejects input with three IDs.
    @Test
    void offerTileAction_DD_rejectsThreeIds() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int ddTileIndex = findOfferTileIndex(match, OfferEffect.DD);

        match.placeTotemOnOfferTile(player, ddTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2,3"));
    }

    // Test that DD fails when one of the two selected IDs does not match a valid Character.
    @Test
    void offerTileAction_DD_rejectsWhenOneIdIsInvalid() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int ddTileIndex = findOfferTileIndex(match, OfferEffect.DD);
        int validCharacterId = nextUnusedCardId(match);
        int missingId = validCharacterId + 1;

        match.placeTotemOnOfferTile(player, ddTileIndex);
        match.getBoard().getBottomRow().add(new Gatherer(validCharacterId, Era.I, CharacterType.GATHERER));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, validCharacterId + "," + missingId));
    }

    // Test that DD fails when one selected ID points to a non-Character card in bottomRow.
    @Test
    void offerTileAction_DD_rejectsWhenOneSelectedCardIsNotCharacter() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int ddTileIndex = findOfferTileIndex(match, OfferEffect.DD);
        int characterId = nextUnusedCardId(match);
        int buildingId = characterId + 1;

        match.placeTotemOnOfferTile(player, ddTileIndex);
        match.getBoard().getBottomRow().add(new Gatherer(characterId, Era.I, CharacterType.GATHERER));
        match.getBoard().getBottomRow().add(new SetCollectionFoodBC(
                buildingId,
                Era.I,
                1,
                1,
                BuildingCardType.SetCollectionFoodBC,
                false
        ));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, characterId + "," + buildingId));
    }

    //! ===================================
    //! 7) offerTileAction(...) - DU
    //! ===================================

    // Test that DU selects one Character from bottomRow and one from topRow.
    @Test
    void offerTileAction_DU_selectsOneBottomAndOneTopCharacter() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duTileIndex = findOfferTileIndex(match, OfferEffect.DU);

        match.placeTotemOnOfferTile(player, duTileIndex);

        Character bottomCharacter = match.getBoard().getBottomRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        Character topCharacter = match.getBoard().getTopRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, bottomCharacter.getId() + "," + topCharacter.getId()));
        assertEquals(ownedBefore + 2, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == bottomCharacter.getId()));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == topCharacter.getId()));
    }

    // Test that DU rejects inputs with the wrong number of IDs.
    @Test
    void offerTileAction_DU_rejectsWrongNumberOfIds() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duTileIndex = findOfferTileIndex(match, OfferEffect.DU);

        match.placeTotemOnOfferTile(player, duTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, ""));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2,3"));
    }

    // Test that DU rejects an invalid bottomRow ID.
    @Test
    void offerTileAction_DU_rejectsInvalidBottomId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duTileIndex = findOfferTileIndex(match, OfferEffect.DU);
        int invalidBottomId = nextUnusedCardId(match);
        Character topCharacter = match.getBoard().getTopRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();

        match.placeTotemOnOfferTile(player, duTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, invalidBottomId + "," + topCharacter.getId()));
    }

    // Test that DU rejects an invalid topRow ID.
    @Test
    void offerTileAction_DU_rejectsInvalidTopId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duTileIndex = findOfferTileIndex(match, OfferEffect.DU);
        Character bottomCharacter = match.getBoard().getBottomRow().stream()
                .filter(Character.class::isInstance)
                .map(Character.class::cast)
                .findFirst()
                .orElseThrow();
        int invalidTopId = nextUnusedCardId(match);

        match.placeTotemOnOfferTile(player, duTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, bottomCharacter.getId() + "," + invalidTopId));
    }

    //! ===================================
    //! 8) offerTileAction(...) - UU
    //! ===================================

    // Test that UU selects two Characters from topRow and assigns both to the player.
    @Test
    void offerTileAction_UU_selectsTwoCharactersFromTopRow() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uuTileIndex = findOfferTileIndex(match, OfferEffect.UU);
        int firstId = nextUnusedCardId(match);
        int secondId = firstId + 1;

        match.placeTotemOnOfferTile(player, uuTileIndex);
        match.getBoard().getTopRow().add(new Gatherer(firstId, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new Gatherer(secondId, Era.I, CharacterType.GATHERER));
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, firstId + "," + secondId));
        assertEquals(ownedBefore + 2, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == firstId));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == secondId));
    }

    // Test that UU rejects inputs with the wrong number of IDs.
    @Test
    void offerTileAction_UU_rejectsWrongNumberOfIds() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uuTileIndex = findOfferTileIndex(match, OfferEffect.UU);

        match.placeTotemOnOfferTile(player, uuTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, ""));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1"));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2,3"));
    }

    // Test that UU rejects an ID that is missing from topRow.
    @Test
    void offerTileAction_UU_rejectsWhenOneIdIsMissing() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uuTileIndex = findOfferTileIndex(match, OfferEffect.UU);
        int validId = nextUnusedCardId(match);
        int missingId = validId + 1;

        match.placeTotemOnOfferTile(player, uuTileIndex);
        match.getBoard().getTopRow().add(new Gatherer(validId, Era.I, CharacterType.GATHERER));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, validId + "," + missingId));
    }

    // Test that UU rejects non-Character cards in topRow.
    @Test
    void offerTileAction_UU_rejectsNonCharacterCard() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int uuTileIndex = findOfferTileIndex(match, OfferEffect.UU);
        int characterId = nextUnusedCardId(match);
        int buildingId = characterId + 1;

        match.placeTotemOnOfferTile(player, uuTileIndex);
        match.getBoard().getTopRow().add(new Gatherer(characterId, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new SetCollectionFoodBC(
                buildingId,
                Era.I,
                1,
                1,
                BuildingCardType.SetCollectionFoodBC,
                false
        ));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, characterId + "," + buildingId));
    }

    //! ===================================
    //! 9) offerTileAction(...) - DUU
    //! ===================================

    // Test that DUU selects one Character from bottomRow and two Characters from topRow.
    @Test
    void offerTileAction_DUU_selectsOneBottomAndTwoTopCharacters() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duuTileIndex = findOfferTileIndex(match, OfferEffect.DUU);
        int bottomId = nextUnusedCardId(match);
        int topId1 = bottomId + 1;
        int topId2 = bottomId + 2;

        match.placeTotemOnOfferTile(player, duuTileIndex);
        match.getBoard().getBottomRow().add(new Gatherer(bottomId, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new Gatherer(topId1, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new Gatherer(topId2, Era.I, CharacterType.GATHERER));
        int ownedBefore = totalOwnedCharacters(player);

        assertDoesNotThrow(() -> match.offerTileAction(player, bottomId + "," + topId1 + "," + topId2));
        assertEquals(ownedBefore + 3, totalOwnedCharacters(player));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card.getId() == bottomId));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == topId1));
        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == topId2));
    }

    // Test that DUU rejects inputs with the wrong number of IDs.
    @Test
    void offerTileAction_DUU_rejectsWrongNumberOfIds() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duuTileIndex = findOfferTileIndex(match, OfferEffect.DUU);

        match.placeTotemOnOfferTile(player, duuTileIndex);

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, ""));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2"));
        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, "1,2,3,4"));
    }

    // Test that DUU rejects an invalid bottomRow ID.
    @Test
    void offerTileAction_DUU_rejectsInvalidBottomId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duuTileIndex = findOfferTileIndex(match, OfferEffect.DUU);
        int invalidBottomId = nextUnusedCardId(match);
        int topId1 = invalidBottomId + 1;
        int topId2 = invalidBottomId + 2;

        match.placeTotemOnOfferTile(player, duuTileIndex);
        match.getBoard().getTopRow().add(new Gatherer(topId1, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new Gatherer(topId2, Era.I, CharacterType.GATHERER));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, invalidBottomId + "," + topId1 + "," + topId2));
    }

    // Test that DUU rejects an invalid topRow ID.
    @Test
    void offerTileAction_DUU_rejectsInvalidTopId() {
        Match match = new Match(createPlayers(5));
        Player player = match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer();
        int duuTileIndex = findOfferTileIndex(match, OfferEffect.DUU);
        int bottomId = nextUnusedCardId(match);
        int validTopId = bottomId + 1;
        int invalidTopId = bottomId + 2;

        match.placeTotemOnOfferTile(player, duuTileIndex);
        match.getBoard().getBottomRow().add(new Gatherer(bottomId, Era.I, CharacterType.GATHERER));
        match.getBoard().getTopRow().add(new Gatherer(validTopId, Era.I, CharacterType.GATHERER));

        assertThrows(IllegalArgumentException.class,
                () -> match.offerTileAction(player, bottomId + "," + validTopId + "," + invalidTopId));
    }

    //! ===================================
    //! 10) endRoundOperations
    //! ===================================

    // Test that bottom events are resolved before the bottom row is cleaned.
    @Test
    void endRoundOperations_resolvesBottomEventsBeforeCleaningRows() {
        Match match = new Match(createPlayers(2));
        Player player = match.getPlayers().get(0);
        player.addCharacter(hunter(900));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        match.getBoard().getBottomRow().add(huntEvent(1000, 2));
        match.getBoard().getBottomRow().add(new SetCollectionFoodBC(1001, Era.I, 1, 1, BuildingCardType.SetCollectionFoodBC, false));
        setMainDeckCards(match,
                List.of(gatherer(2000, Era.I), gatherer(2001, Era.I), gatherer(2002, Era.I), gatherer(2003, Era.I), gatherer(2004, Era.I), gatherer(2005, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card instanceof EventCard));
        assertTrue(match.getBoard().getBottomRow().stream().anyMatch(card -> card instanceof BuildingCard));
    }

    // Test that bottom-row Characters and Events are discarded while Buildings remain.
    @Test
    void endRoundOperations_discardsCharactersAndEventsFromBottomRow() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        match.getBoard().getBottomRow().add(gatherer(1020, Era.I));
        match.getBoard().getBottomRow().add(huntEvent(1021, 1));
        match.getBoard().getBottomRow().add(new SetCollectionFoodBC(1022, Era.I, 1, 1, BuildingCardType.SetCollectionFoodBC, false));
        setMainDeckCards(match,
                List.of(gatherer(2200, Era.I), gatherer(2201, Era.I), gatherer(2202, Era.I), gatherer(2203, Era.I), gatherer(2204, Era.I), gatherer(2205, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertEquals(1, match.getBoard().getBottomRow().stream().filter(card -> card instanceof BuildingCard).count());
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card instanceof Character));
        assertTrue(match.getBoard().getBottomRow().stream().noneMatch(card -> card instanceof Sustenance || card instanceof HuntEvent || card instanceof CavePainting || card instanceof ShamanicRitual));
    }

    // Test that top-row Characters and Events move to the bottom row.
    @Test
    void endRoundOperations_movesCharactersAndEventsFromTopToBottom() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        match.getBoard().getTopRow().add(gatherer(1030, Era.I));
        match.getBoard().getTopRow().add(huntEvent(1031, 1));
        match.getBoard().getTopRow().add(new SetCollectionFoodBC(1032, Era.I, 1, 1, BuildingCardType.SetCollectionFoodBC, false));
        setMainDeckCards(match,
                List.of(gatherer(2300, Era.I), gatherer(2301, Era.I), gatherer(2302, Era.I), gatherer(2303, Era.I), gatherer(2304, Era.I), gatherer(2305, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertTrue(match.getBoard().getTopRow().stream().noneMatch(card -> card.getId() == 1030 || card.getId() == 1031));
        assertTrue(match.getBoard().getBottomRow().stream().anyMatch(card -> card.getId() == 1030));
        assertTrue(match.getBoard().getBottomRow().stream().anyMatch(card -> card.getId() == 1031));
    }

    // Test that moved top-row cards are inserted before the existing bottom-row buildings.
    @Test
    void endRoundOperations_movesTopCardsToLeftOfBottomBuildings() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        match.getBoard().getTopRow().add(gatherer(1040, Era.I));
        match.getBoard().getTopRow().add(huntEvent(1041, 1));
        match.getBoard().getBottomRow().add(new SetCollectionFoodBC(1042, Era.I, 1, 1, BuildingCardType.SetCollectionFoodBC, false));
        setMainDeckCards(match,
                List.of(gatherer(2400, Era.I), gatherer(2401, Era.I), gatherer(2402, Era.I), gatherer(2403, Era.I), gatherer(2404, Era.I), gatherer(2405, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertEquals(1040, match.getBoard().getBottomRow().get(0).getId());
        assertEquals(1041, match.getBoard().getBottomRow().get(1).getId());
        assertEquals(1042, match.getBoard().getBottomRow().get(match.getBoard().getBottomRow().size() - 1).getId());
    }

    // Test that the top row is refilled with exactly players.size() + 4 cards.
    @Test
    void endRoundOperations_refillsTopRowByPlayersPlusFourCards() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        setMainDeckCards(match,
                List.of(gatherer(2500, Era.I), gatherer(2501, Era.I), gatherer(2502, Era.I), gatherer(2503, Era.I), gatherer(2504, Era.I), gatherer(2505, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertEquals(6, match.getBoard().getTopRow().size());
    }

    // Test that new drawn cards are inserted to the left of existing top-row buildings.
    @Test
    void endRoundOperations_addsNewDrawnCardsToLeftOfExistingTopBuildings() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        match.getBoard().getTopRow().add(new SetCollectionFoodBC(1050, Era.I, 1, 1, BuildingCardType.SetCollectionFoodBC, false));
        setMainDeckCards(match,
                List.of(gatherer(2600, Era.I), gatherer(2601, Era.I), gatherer(2602, Era.I), gatherer(2603, Era.I), gatherer(2604, Era.I), gatherer(2605, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertEquals(7, match.getBoard().getTopRow().size());
        assertInstanceOf(BuildingCard.class, match.getBoard().getTopRow().get(match.getBoard().getTopRow().size() - 1));
    }

    // Test that drawing a new era card advances the current era.
    @Test
    void endRoundOperations_advancesEraWhenDrawingNewEraCard() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        setMainDeckCards(match,
                List.of(gatherer(2700, Era.I), gatherer(2701, Era.I), gatherer(2702, Era.I), gatherer(2703, Era.I), gatherer(2704, Era.I)),
                List.of(gatherer(2705, Era.II)),
                List.of());

        match.endRoundOperations();

        assertEquals(Era.II, match.getGameState().getCurrentEra());
    }

    // Test that turn order is rebuilt from offer track order.
    @Test
    void endRoundOperations_rebuildsTurnOrderFromOfferTrack() {
        Match match = new Match(createPlayers(2));
        Player first = match.getPlayers().get(0);
        Player second = match.getPlayers().get(1);

        match.placeTotemOnOfferTile(second, 1);
        match.placeTotemOnOfferTile(first, 2);
        List<Player> expectedOrder = List.of(
                match.getBoard().getOfferTrack().get(0).getPlayer(),
                match.getBoard().getOfferTrack().get(1).getPlayer()
        );

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        setMainDeckCards(match,
                List.of(gatherer(2800, Era.I), gatherer(2801, Era.I), gatherer(2802, Era.I), gatherer(2803, Era.I), gatherer(2804, Era.I), gatherer(2805, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertSame(expectedOrder.get(0), match.getBoard().getTurnOrderTile().getSlots().get(0).getPlayer());
        assertSame(expectedOrder.get(1), match.getBoard().getTurnOrderTile().getSlots().get(1).getPlayer());
    }

    // Test that offer tiles are cleared after end-of-round operations.
    @Test
    void endRoundOperations_clearsPlayersFromOfferTrack() {
        Match match = new Match(createPlayers(2));
        Player first = match.getPlayers().get(0);
        Player second = match.getPlayers().get(1);

        match.placeTotemOnOfferTile(first, 1);
        match.placeTotemOnOfferTile(second, 2);

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        setMainDeckCards(match,
                List.of(gatherer(2900, Era.I), gatherer(2901, Era.I), gatherer(2902, Era.I), gatherer(2903, Era.I), gatherer(2904, Era.I), gatherer(2905, Era.I)),
                List.of(),
                List.of());

        match.endRoundOperations();

        assertTrue(match.getBoard().getOfferTrack().stream().allMatch(tile -> tile.getPlayer() == null));
    }

    // Test that an empty main deck currently causes endRoundOperations to throw.
    @Test
    void endRoundOperations_whenMainDeckIsEmpty_behaviorIsExplicit() {
        Match match = new Match(createPlayers(2));

        match.getBoard().getTopRow().clear();
        match.getBoard().getBottomRow().clear();
        setMainDeckCards(match, List.of(), List.of(), List.of());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, match::endRoundOperations);
        assertEquals("No cards left in deck", exception.getMessage());
    }









}