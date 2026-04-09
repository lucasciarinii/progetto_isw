package org.example.model.cards.buildingCards;

import org.example.model.cards.characters.Artist;
import org.example.model.cards.characters.Builder;
import org.example.model.cards.characters.Gatherer;
import org.example.model.cards.characters.Hunter;
import org.example.model.cards.characters.Inventor;
import org.example.model.cards.characters.Shaman;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterEndPointsBCTest {

	@Test
	@DisplayName("Zero requested characters: points do not change")
	void applyEffect_zeroRequestedCharacters_pointsRemainUnchanged() {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(1, Era.I, CharacterType.ARTIST));

		CharacterEndPointsBC building = new CharacterEndPointsBC(2, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, 3, CharacterType.HUNTER, false);

		building.applyEffect(player, match);

		assertEquals(0, player.getPoints());
	}

	@Test
	@DisplayName("One requested character: points increase by pointsEffect")
	void applyEffect_oneRequestedCharacter_pointsIncreaseByEffect() {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Hunter(3, Era.I, CharacterType.HUNTER, false));

		CharacterEndPointsBC building = new CharacterEndPointsBC(4, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, 5, CharacterType.HUNTER, false);

		building.applyEffect(player, match);

		assertEquals(5, player.getPoints());
	}

	@Test
	@DisplayName("Four requested characters (artists): points increase linearly")
	void applyEffect_fourRequestedCharacters_pointsIncreaseByEight() {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Artist(5, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(6, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(7, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(8, Era.I, CharacterType.ARTIST));

		CharacterEndPointsBC building = new CharacterEndPointsBC(9, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, 2, CharacterType.ARTIST, false);

		building.applyEffect(player, match);

		assertEquals(8, player.getPoints());
	}

	@Test
	@DisplayName("Non-matching characters are ignored")
	void applyEffect_nonMatchingCharacters_pointsRemainUnchanged() {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		player.addCharacter(new Inventor(10, Era.I, CharacterType.INVENTOR, InventionType.BOAT));
		player.addCharacter(new Inventor(11, Era.I, CharacterType.INVENTOR, InventionType.ARROW));
		player.addCharacter(new Inventor(12, Era.I, CharacterType.INVENTOR, InventionType.HOOK));
		player.addCharacter(new Artist(13, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Artist(14, Era.I, CharacterType.ARTIST));
		player.addCharacter(new Hunter(15, Era.I, CharacterType.HUNTER, false));

		CharacterEndPointsBC building = new CharacterEndPointsBC(16, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, 4, CharacterType.GATHERER, false);
        player.addPoints(-player.getPoints()); // Reset points to 0 for clarity

        building.applyEffect(player, match);

		assertEquals(0, player.getPoints());
	}

	@Test
	@DisplayName("Points are added to the existing total")
	void applyEffect_pointsAreAddedToTheExistingTotal() {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

        player.addPoints(-player.getPoints()); // Reset points to 0 for clarity
		player.addPoints(7);
		player.addCharacter(new Builder(17, Era.I, CharacterType.BUILDER, 0, 0));
		player.addCharacter(new Builder(18, Era.I, CharacterType.BUILDER, 0, 0));

		CharacterEndPointsBC building = new CharacterEndPointsBC(19, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, 3, CharacterType.BUILDER, false);

		building.applyEffect(player, match);

		assertEquals(13, player.getPoints());
	}

	@ParameterizedTest(name = "{0} with {1} matching characters and pointsEffect {2} gives {3} points")
	@MethodSource("pointsScalingCases")
	void applyEffect_scalesLinearlyWithMatchingCharacters(CharacterType characterEffect, int matchingCharacters, int pointsEffect, int expectedPoints) {
		Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
		Player player = match.getPlayers().get(0);

		addMatchingCharacters(player, characterEffect, matchingCharacters);

		CharacterEndPointsBC building = new CharacterEndPointsBC(20, Era.I, 0, 0, BuildingCardType.CharacterEndPointsBC, pointsEffect, characterEffect, false);

		building.applyEffect(player, match);

		assertEquals(expectedPoints, player.getPoints());
	}

	private static Stream<Arguments> pointsScalingCases() {
		return Stream.of(
				Arguments.of(CharacterType.INVENTOR, 2, 3, 6),
				Arguments.of(CharacterType.GATHERER, 3, 2, 6),
				Arguments.of(CharacterType.SHAMAN, 1, 4, 4),
				Arguments.of(CharacterType.BUILDER, 2, 5, 10),
				Arguments.of(CharacterType.ARTIST, 4, 2, 8),
				Arguments.of(CharacterType.HUNTER, 3, 1, 3)
		);
	}

	private void addMatchingCharacters(Player player, CharacterType characterType, int count) {
		for (int i = 0; i < count; i++) {
			player.addCharacter(createCharacter(characterType, 100 + i));
		}
	}

	private org.example.model.cards.characters.Character createCharacter(CharacterType characterType, int id) {
		return switch (characterType) {
			case INVENTOR -> new Inventor(id, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
			case GATHERER -> new Gatherer(id, Era.I, CharacterType.GATHERER);
			case SHAMAN -> new Shaman(id, Era.I, CharacterType.SHAMAN, 0);
			case BUILDER -> new Builder(id, Era.I, CharacterType.BUILDER, 0, 0);
			case ARTIST -> new Artist(id, Era.I, CharacterType.ARTIST);
			case HUNTER -> new Hunter(id, Era.I, CharacterType.HUNTER, false);
		};
	}
}