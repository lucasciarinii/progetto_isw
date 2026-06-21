package it.polimi.ingsw.server.model.board;

import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSlotTest {

	// Verifies that constructor stores a null player and preserves food/points values.
	@Test
	void constructor_withNullPlayer_initializesSlotState() {
		PlayerSlot slot = new PlayerSlot(null, 2, -1);

		assertNull(slot.getPlayer());
		assertEquals(2, slot.getFood());
		assertEquals(-1, slot.getPoints());
	}

	// Verifies that constructor stores the provided player reference as initial occupant.
	@Test
	void constructor_withPlayer_initializesOccupiedSlot() {
		Player initialPlayer = new Player("alice");
		PlayerSlot slot = new PlayerSlot(initialPlayer, 0, -3);

		assertSame(initialPlayer, slot.getPlayer());
		assertEquals(0, slot.getFood());
		assertEquals(-3, slot.getPoints());
	}


	// Verifies that removeTotem clears an occupied slot.
	@Test
	void removeTotem_onOccupiedSlot_clearsOccupant() {
		PlayerSlot slot = new PlayerSlot(new Player("alice"), 1, -1);

		slot.removeTotem();

		assertNull(slot.getPlayer());
	}

	// Verifies that removeTotem is idempotent and safe on an already empty slot.
	@Test
	void removeTotem_onEmptySlot_keepsSlotEmpty() {
		PlayerSlot slot = new PlayerSlot(null, 1, -2);

		slot.removeTotem();

		assertNull(slot.getPlayer());
	}

	// Verifies that null input is rejected before any state change.
	@Test
	void placePlayerAndApplyEffect_withNullPlayer_throwsNullPointerException() {
		PlayerSlot slot = new PlayerSlot(null, 1, -4);

		assertThrows(NullPointerException.class, () -> slot.placePlayerAndApplyEffect(null));
		assertNull(slot.getPlayer());
	}

	// Verifies that placing a player on an occupied slot is rejected.
	@Test
	void placePlayerAndApplyEffect_onOccupiedSlot_throwsIllegalArgumentException() {
		Player existing = new Player("keeper");
		Player incoming = new Player("incoming");
		PlayerSlot slot = new PlayerSlot(existing, 2, -1);

		assertThrows(IllegalArgumentException.class, () -> slot.placePlayerAndApplyEffect(incoming));
		assertSame(existing, slot.getPlayer());
		assertEquals(0, incoming.getFood());
		assertEquals(0, incoming.getPoints());
	}

	// Verifies that a player is moved into the slot and positive food bonus is applied.
	@Test
	void placePlayerAndApplyEffect_onEmptySlot_placesPlayerAndAddsFood() {
		Player incoming = new Player("incoming");
		PlayerSlot slot = new PlayerSlot(null, 3, -2);

		slot.placePlayerAndApplyEffect(incoming);

		assertSame(incoming, slot.getPlayer());
		assertEquals(3, incoming.getFood());
		assertEquals(0, incoming.getPoints());
	}

	// Verifies that negative food is applied as a penalty when player has at least 1 food.
	@Test
	void placePlayerAndApplyEffect_negativeFoodWithAvailableFood_appliesFoodPenalty() {
		Player incoming = new Player("incoming");
		incoming.addFood(2);
		PlayerSlot slot = new PlayerSlot(null, -1, -3);

		slot.placePlayerAndApplyEffect(incoming);

		assertSame(incoming, slot.getPlayer());
		assertEquals(1, incoming.getFood());
		assertEquals(0, incoming.getPoints());
	}

	// Verifies the special branch: if food is negative and player has no food, points are applied instead.
	@Test
	void placePlayerAndApplyEffect_negativeFoodWithoutAvailableFood_appliesPointsInstead() {
		Player incoming = new Player("incoming");
		PlayerSlot slot = new PlayerSlot(null, -2, -4);

		slot.placePlayerAndApplyEffect(incoming);

		assertSame(incoming, slot.getPlayer());
		assertEquals(0, incoming.getFood());
		assertEquals(-4, incoming.getPoints());
	}
}