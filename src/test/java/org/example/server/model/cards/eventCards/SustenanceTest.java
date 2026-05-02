package org.example.server.model.cards.eventCards;

import org.example.client.view.tui.ConsoleColors;
import org.example.server.model.cards.characters.*;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.enums.InventionType;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SustenanceTest {

    // Test: applyEvent with null match should throw NullPointerException
    @Test
	@DisplayName("applyEvent(null) throws NullPointerException")
	void applyEvent_nullMatch_throwsNullPointerException() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		assertThrows(NullPointerException.class, () -> sustenance.applyEvent(null));
	}

    // Test: applyEvent with a player having zero characters and zero food should keep food and points unchanged
	@Test
	@DisplayName("applyEvent with zero characters and zero food keeps food and points unchanged")
	void applyEvent_noCharactersNoFood_doesNothing() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addFood(-player.getFood()); // zero food

		int pointsBefore = player.getPoints();
		int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore, player.getPoints());
		assertEquals(foodBefore, player.getFood());
	}

    // Test: applyEvent with a player having zero characters and positive food should keep food and points unchanged
    @Test
	@DisplayName("applyEvent with zero characters and positive food keeps food and points unchanged")
	void applyEvent_noCharactersFiveFood_doesNothing() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(java.util.List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addFood(3);

		int pointsBefore = player.getPoints();
		int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore, player.getPoints());
		assertEquals(foodBefore, player.getFood());
	}

    // Test: applyEvent with exactly enough food for all characters consumes all food without changing points
    @Test
	@DisplayName("applyEvent with exactly enough food consumes all food and keeps points unchanged")
	void applyEvent_exactlyEnoughFood_consumesAllFoodWithoutChangingPoints() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));

		player.addFood(-player.getFood()); // reset food to 0
		player.addFood(4); // food becomes exactly 4

		int pointsBefore = player.getPoints();
		int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore, player.getPoints());
		assertEquals(foodBefore - 4, player.getFood());
		assertEquals(0, player.getFood());
	}

    // Test: applyEvent with more than enough food for all characters consumes food without changing points
    @Test
	@DisplayName("applyEvent with more than enough food consumes food and keeps points unchanged")
	void applyEvent_moreThanEnoughFood_consumesAllFoodWithoutChangingPoints() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));

		player.addFood(-player.getFood()); // reset food to 0
		player.addFood(10); // food becomes exactly 10

		int pointsBefore = player.getPoints();
		int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore, player.getPoints());
		assertEquals(foodBefore - 4, player.getFood());
		assertEquals(6, player.getFood());
	}

    // Test: applyEvent with -1 insufficient food, with changing points (-2)
    @Test
	@DisplayName("applyEvent with -1 insufficient food, with changing points (-2)")
	void applyEvent_insufficientFood_consumesAllFoodAndChangingPoints() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));

		player.addFood(-player.getFood()); // reset food to 0
		player.addFood(3); // food becomes exactly 3 which is 1 less than needed

		int pointsBefore = player.getPoints();
		//int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore - 2, player.getPoints());
		assertEquals(0, player.getFood());
	}

    // Test: applyEvent with more insufficient food, with changing points (-2 * character not sustained) and covering more character types
    @Test
	@DisplayName("applyEvent with more insufficient food, with changing points (-2 * character not sustained)")
	void applyEvent_moreInsufficientFood_consumesAllFoodAndChangingPoints() {
		Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Hunter(1001, Era.I, CharacterType.HUNTER, false));
		player.addCharacter(new Shaman(1002, Era.I, CharacterType.SHAMAN, 2));
		player.addCharacter(new Inventor(1004, Era.I, CharacterType.INVENTOR, InventionType.ARROW));
		player.addCharacter(new Builder(1005, Era.I, CharacterType.BUILDER, -1, 3));
		player.addCharacter(new Artist(1006, Era.I, CharacterType.ARTIST));

		player.addFood(-player.getFood()); // reset food to 0
		player.addFood(2); // food becomes exactly 2 which is 4 less than needed, so 4 characters are not sustained, which is -2 points each, so -8 points total

		int pointsBefore = player.getPoints();
		//int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore - (2 * 4), player.getPoints());
		assertEquals(0, player.getFood());
	}

    // Test: applyEvent with exact discount (food and points unchanged)
    @Test
    @DisplayName("applyEvent with exact discount, with changing points and food")
    void applyEvent_gatherersOptions1() {
        Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        player.addCharacter(new Gatherer(1000, Era.I, CharacterType.GATHERER));
        player.addCharacter(new Gatherer(1001, Era.I, CharacterType.GATHERER));
        player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));
        // In this case the player has 2 gatherers which should give him a discount of 6 on sustenance, so he only needs to pay 6 - 6 = 0 food

        // Let's start from 3 food
        player.addFood(-player.getFood()); // reset food to 0
        player.addFood(3); // food becomes exactly 3 which is 1 less than needed

        int pointsBefore = player.getPoints();
        int foodBefore = player.getFood();

        sustenance.applyEvent(match);

        assertEquals(pointsBefore, player.getPoints());
        assertEquals(foodBefore, player.getFood());
    }

    // Test: applyEvent with discount applied and just pay food without changing points
    @Test
    @DisplayName("applyEvent with discount applied and just pay food without changing points")
    void applyEvent_gatherersOptions2() {
        Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        player.addCharacter(new Gatherer(1000, Era.I, CharacterType.GATHERER));
        player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));
        // In this case the player has 1 gatherer which should give him a discount of 3 on sustenance, so he only needs to pay 5 - 3 = 2 food; points remain unchanged

        // Let's start from 3 food
        player.addFood(-player.getFood()); // reset food to 0
        player.addFood(3); // food becomes exactly 3 which is 1 less than needed

        int pointsBefore = player.getPoints();
        int foodBefore = player.getFood();

        sustenance.applyEvent(match);

        assertEquals(pointsBefore, player.getPoints());
        assertEquals(foodBefore - 2, player.getFood());
    }

    // Test: applyEvent with discount applied and pay both food and points (because food is still insufficient after discount)
    @Test
    @DisplayName("applyEvent with discount applied and pay both food and points")
    void applyEvent_gatherersOptions3() {
        Sustenance sustenance = new Sustenance(1, Era.I, false, EventEffect.SUSTENANCE, 2);

        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        player.addCharacter(new Gatherer(1000, Era.I, CharacterType.GATHERER));
        player.addCharacter(new Artist(1000, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1001, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1002, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1003, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1004, Era.I, CharacterType.ARTIST));
        player.addCharacter(new Artist(1005, Era.I, CharacterType.ARTIST));
        // In this case the player has 1 gatherer which should give him a discount of 3 on sustenance, so he needs to pay 7 - 3 = 4 food;

        // Let's start from 2 food, this means that remains 2 food to pay, so the player should lose 2 * 2 = 4 points (and all food)
        player.addFood(-player.getFood()); // reset food to 0
        player.addFood(2); // food becomes exactly 3 which is 1 less than needed

        int pointsBefore = player.getPoints();
        int foodBefore = player.getFood();

        sustenance.applyEvent(match);

        assertEquals(pointsBefore - (2 * 2), player.getPoints());
        assertEquals(0, player.getFood());
    }

	//Test that isSustenance returns true.
	@Test
	@DisplayName("isSustenance returns true")
	void isSustenance_returnsTrue() {
		Sustenance sustenance = new Sustenance(10, Era.I, false, EventEffect.SUSTENANCE, 3);

		assertTrue(sustenance.isSustenance());
	}

	//Test that toString contains the sustenance description and points value.
	@Test
	@DisplayName("toString contains sustenance text and points")
	void toString_containsExpectedText() {
		Sustenance sustenance = new Sustenance(11, Era.II, false, EventEffect.SUSTENANCE, 4);

		String result = sustenance.toString();

		assertTrue(result.contains("pay 1 food for each character card OR"));
		assertTrue(result.contains("pay 4 points for it"));
		assertTrue(result.endsWith(ConsoleColors.RESET + "\n"));
	}

	//Test that applyEvent clamps the food cost to zero when discount exceeds total characters.
	@Test
	@DisplayName("applyEvent clamps negative sustenance cost to zero")
	void applyEvent_discountGreaterThanCharacters_clampsCostToZero() {
		Sustenance sustenance = new Sustenance(12, Era.I, false, EventEffect.SUSTENANCE, 2);

		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addFood(-player.getFood());
		player.addFood(5);

		//One Gatherer is one character, but usually gives discount 3 through Player logic.
		//So totalCharacterToPay becomes 1 - 3 = -2 and must be clamped to 0.
		player.addCharacter(new Gatherer(2000, Era.I, CharacterType.GATHERER));

		int pointsBefore = player.getPoints();
		int foodBefore = player.getFood();

		sustenance.applyEvent(match);

		assertEquals(pointsBefore, player.getPoints());
		assertEquals(foodBefore, player.getFood());
	}
}
