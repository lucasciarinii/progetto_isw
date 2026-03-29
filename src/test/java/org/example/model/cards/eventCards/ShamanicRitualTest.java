package org.example.model.cards.eventCards;

import org.example.model.cards.buildingCards.ShamanicDoublePointsBC;
import org.example.model.cards.buildingCards.ShamanicNoMalusBC;
import org.example.model.cards.buildingCards.ShamanicStarsBC;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.enums.EventEffect;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShamanicRitualTest {

	private static final int BONUS_POINTS = 4;
	private static final int MALUS_POINTS = -2;

	private final ShamanicRitual shamanicRitual = new ShamanicRitual(
			999,
			Era.I,
			false,
			EventEffect.SHAMANIC_RITUAL,
			BONUS_POINTS,
			MALUS_POINTS
	);

	@Test
	void applyEventThrowsWhenMatchIsNull() {
		assertThrows(NullPointerException.class, () -> shamanicRitual.applyEvent(null));
	}

	@Test
	void applyEventAwardsAndPenalizesPlayersWithDifferentShamanStars() {
		Player winner = playerWithStars("winner", 3);
		Player loser = playerWithStars("loser", 1);

		Match match = createMatch(winner, loser);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, winner.getPoints());
		assertEquals(MALUS_POINTS, loser.getPoints());
	}

	@Test
	void applyEventAwardsDoublePointsToUniqueWinnerWithShamanicDoublePointsBuilding() {
		Player winner = playerWithStars("winner", 3);
		winner.addBuilding(new ShamanicDoublePointsBC(1, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));

		Player loser = playerWithStars("loser", 1);

		Match match = createMatch(winner, loser);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS * 2, winner.getPoints());
		assertEquals(MALUS_POINTS, loser.getPoints());
	}

	@Test
	void applyEventDoesNotPenalizeUniqueLoserWithShamanicNoMalusBuilding() {
		Player winner = playerWithStars("winner", 3);
		Player protectedLoser = playerWithStars("protectedLoser", 1);
		protectedLoser.addBuilding(new ShamanicNoMalusBC(2, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));

		Match match = createMatch(winner, protectedLoser);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, winner.getPoints());
		assertEquals(0, protectedLoser.getPoints());
	}

	@Test
	void applyEventHandlesWinnerWithDoublePointsAndLoserWithNoMalusBuilding() {
		Player winner = playerWithStars("winner", 3);
		winner.addBuilding(new ShamanicDoublePointsBC(3, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));

		Player protectedLoser = playerWithStars("protectedLoser", 1);
		protectedLoser.addBuilding(new ShamanicNoMalusBC(4, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));

		Match match = createMatch(winner, protectedLoser);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS * 2, winner.getPoints());
		assertEquals(0, protectedLoser.getPoints());
	}

	@Test
	void applyEventDoesNotAwardDoublePointsWhenDoublePointsOwnerTiesForMaximumStars() {
		Player doubleOwner = playerWithStars("doubleOwner", 3);
		doubleOwner.addBuilding(new ShamanicDoublePointsBC(5, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));

		Player tiedWinner = playerWithStars("tiedWinner", 3);

		Match match = createMatch(doubleOwner, tiedWinner);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, doubleOwner.getPoints());
		assertEquals(BONUS_POINTS, tiedWinner.getPoints());
	}

	@Test
	void applyEventStillLetsNoMalusOwnerWinWhenItHasMaximumStars() {
		Player winnerWithNoMalus = playerWithStars("winnerWithNoMalus", 3);
		winnerWithNoMalus.addBuilding(new ShamanicNoMalusBC(6, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));

		Player loser = playerWithStars("loser", 1);

		Match match = createMatch(winnerWithNoMalus, loser);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, winnerWithNoMalus.getPoints());
		assertEquals(MALUS_POINTS, loser.getPoints());
	}

	@Test
	void shamanicStarsBuildingCanTurnOutcomeIntoVictory() {
		Player boostedPlayer = playerWithStars("boosted", 1);
		Player opponent = playerWithStars("opponent", 2);

		ShamanicStarsBC shamanicStarsBC = new ShamanicStarsBC(
				7,
				Era.I,
				0,
				0,
				BuildingCardType.ShamanicStarsBC,
				false
		);

		shamanicStarsBC.applyEffect(boostedPlayer, null);

		Match match = createMatch(boostedPlayer, opponent);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, boostedPlayer.getPoints());
		assertEquals(MALUS_POINTS, opponent.getPoints());
	}

	@Test
	void shamanicStarsBuildingCanTurnOutcomeIntoTie() {
		Player boostedPlayer = new Player("boosted");
		Player opponent = playerWithStars("opponent", 3);

		ShamanicStarsBC shamanicStarsBC = new ShamanicStarsBC(
				8,
				Era.I,
				0,
				0,
				BuildingCardType.ShamanicStarsBC,
				false
		);

		shamanicStarsBC.applyEffect(boostedPlayer, null);

		Match match = createMatch(boostedPlayer, opponent);
		shamanicRitual.applyEvent(match);

		assertEquals(BONUS_POINTS, boostedPlayer.getPoints());
		assertEquals(BONUS_POINTS, opponent.getPoints());
	}

	@Test
	void applyEventAwardsBonusToAllPlayersWhenEveryoneIsTiedFrom2To5Players() {
		for (int playersCount = 2; playersCount <= 5; playersCount++) {
			Player[] players = new Player[playersCount];

			for (int i = 0; i < playersCount; i++) {
				players[i] = playerWithStars("player_" + playersCount + "_" + i, 2);
			}

			Match match = createMatch(players);
			shamanicRitual.applyEvent(match);

			for (Player player : players) {
				assertEquals(BONUS_POINTS, player.getPoints());
			}
		}
	}

	@Test
	void shamanicStarsOwnerWinsBonusAndAllTiedLowestPlayersLosePoints() {
		for (int playersCount = 3; playersCount <= 5; playersCount++) {
			Player[] players = new Player[playersCount];

			players[0] = playerWithStars("boosted_" + playersCount, 1);
			for (int i = 1; i < playersCount; i++) {
				players[i] = playerWithStars("lowTied_" + playersCount + "_" + i, 1);
			}

			ShamanicStarsBC shamanicStarsBC = new ShamanicStarsBC(
					100 + playersCount,
					Era.I,
					0,
					0,
					BuildingCardType.ShamanicStarsBC,
					false
			);
			shamanicStarsBC.applyEffect(players[0], null);

			Match match = createMatch(players);
			shamanicRitual.applyEvent(match);

			assertEquals(BONUS_POINTS, players[0].getPoints());
			for (int i = 1; i < playersCount; i++) {
				assertEquals(MALUS_POINTS, players[i].getPoints());
			}
		}
	}

	private Player playerWithStars(String nickname, int stars) {
		Player player = new Player(nickname);
		player.addShamanStars(stars);
		return player;
	}

	private Match createMatch(Player... players) {
		return new Match(new ArrayList<>(Arrays.asList(players)));
	}

}