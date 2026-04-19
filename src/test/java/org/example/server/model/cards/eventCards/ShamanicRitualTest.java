package org.example.server.model.cards.eventCards;

import org.example.server.model.cards.buildingCards.ShamanicDoublePointsBC;
import org.example.server.model.cards.buildingCards.ShamanicNoMalusBC;
import org.example.server.model.cards.buildingCards.ShamanicStarsBC;
import org.example.server.model.cards.eventCards.ShamanicRitual;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	// Verifies that applyEvent rejects a null match.
	@Test
	void applyEventThrowsWhenMatchIsNull() {
		assertThrows(NullPointerException.class, () -> shamanicRitual.applyEvent(null));
	}

	// Verifies the base case: highest stars gets bonus, lowest stars gets Malus.
	@Test
	void applyEventAwardsAndPenalizesPlayersWithDifferentShamanStars() {
		Player winner = playerWithStars("winner", 3);
		Player loser = playerWithStars("loser", 1);
		winner.addPoints(10);
		loser.addPoints(7);

		Match match = createMatch(winner, loser);
		int beforeWinnerPoints = winner.getPoints();
		int beforeLoserPoints = loser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeWinnerPoints + BONUS_POINTS, winner.getPoints());
		assertEquals(beforeLoserPoints + MALUS_POINTS, loser.getPoints());
	}

	// Verifies double bonus for a unique winner owning ShamanicDoublePointsBC.
	@Test
	void applyEventAwardsDoublePointsToUniqueWinnerWithShamanicDoublePointsBuilding() {
		Player winner = playerWithStars("winner", 3);
		winner.addBuilding(new ShamanicDoublePointsBC(1, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));
		winner.addPoints(12);

		Player loser = playerWithStars("loser", 1);
		loser.addPoints(9);

		Match match = createMatch(winner, loser);
		int beforeWinnerPoints = winner.getPoints();
		int beforeLoserPoints = loser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeWinnerPoints + BONUS_POINTS * 2, winner.getPoints());
		assertEquals(beforeLoserPoints + MALUS_POINTS, loser.getPoints());
	}

	// Verifies that a unique loser with ShamanicNoMalusBC does not lose points.
	@Test
	void applyEventDoesNotPenalizeUniqueLoserWithShamanicNoMalusBuilding() {
		Player winner = playerWithStars("winner", 3);
		Player protectedLoser = playerWithStars("protectedLoser", 1);
		protectedLoser.addBuilding(new ShamanicNoMalusBC(2, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));
		winner.addPoints(8);
		protectedLoser.addPoints(6);

		Match match = createMatch(winner, protectedLoser);
		int beforeWinnerPoints = winner.getPoints();
		int beforeLoserPoints = protectedLoser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeWinnerPoints + BONUS_POINTS, winner.getPoints());
		assertEquals(beforeLoserPoints, protectedLoser.getPoints());
	}

	// Verifies mixed special cards: winner doubles bonus, loser is protected from Malus.
	@Test
	void applyEventHandlesWinnerWithDoublePointsAndLoserWithNoMalusBuilding() {
		Player winner = playerWithStars("winner", 3);
		winner.addBuilding(new ShamanicDoublePointsBC(3, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));
		winner.addPoints(11);

		Player protectedLoser = playerWithStars("protectedLoser", 1);
		protectedLoser.addBuilding(new ShamanicNoMalusBC(4, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));
		protectedLoser.addPoints(5);

		Match match = createMatch(winner, protectedLoser);
		int beforeWinnerPoints = winner.getPoints();
		int beforeLoserPoints = protectedLoser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeWinnerPoints + BONUS_POINTS * 2, winner.getPoints());
		assertEquals(beforeLoserPoints, protectedLoser.getPoints());
	}

	// Verifies no doubling when two players tie for maximum stars, while a distinct lowest player gets Malus.
	@Test
	void applyEventDoesNotAwardDoublePointsWhenDoublePointsOwnerTiesForMaximumStarsWithThirdPlayerLosingPoints() {
		Player doubleOwner = playerWithStars("doubleOwner", 3);
		doubleOwner.addBuilding(new ShamanicDoublePointsBC(5, Era.I, 0, 0, BuildingCardType.ShamanicDoublePointsBC, false));

		Player tiedWinner = playerWithStars("tiedWinner", 3);
		Player loser = playerWithStars("loser", 1);

		Match match = createMatch(doubleOwner, tiedWinner, loser);
		int beforeDoubleOwnerPoints = doubleOwner.getPoints();
		int beforeTiedWinnerPoints = tiedWinner.getPoints();
		int beforeLoserPoints = loser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeDoubleOwnerPoints + BONUS_POINTS, doubleOwner.getPoints());
		assertEquals(beforeTiedWinnerPoints + BONUS_POINTS, tiedWinner.getPoints());
		assertEquals(beforeLoserPoints + MALUS_POINTS, loser.getPoints());
	}

	// Verifies that ShamanicNoMalusBC does not prevent a winning player from receiving bonus.
	@Test
	void applyEventStillLetsNoMalusOwnerWinWhenItHasMaximumStars() {
		Player winnerWithNoMalus = playerWithStars("winnerWithNoMalus", 3);
		winnerWithNoMalus.addBuilding(new ShamanicNoMalusBC(6, Era.I, 0, 0, BuildingCardType.ShamanicNoMalusBC, false));
		winnerWithNoMalus.addPoints(15);

		Player loser = playerWithStars("loser", 1);
		loser.addPoints(2);

		Match match = createMatch(winnerWithNoMalus, loser);
		int beforeWinnerPoints = winnerWithNoMalus.getPoints();
		int beforeLoserPoints = loser.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeWinnerPoints + BONUS_POINTS, winnerWithNoMalus.getPoints());
		assertEquals(beforeLoserPoints + MALUS_POINTS, loser.getPoints());
	}

	// Verifies that ShamanicStarsBC can turn a losing player into the unique winner.
	@Test
	void shamanicStarsBuildingCanTurnOutcomeIntoVictory() {
		Player boostedPlayer = playerWithStars("boosted", 1);
		Player opponent = playerWithStars("opponent", 2);
		boostedPlayer.addPoints(14);
		opponent.addPoints(3);

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
		int beforeBoostedPoints = boostedPlayer.getPoints();
		int beforeOpponentPoints = opponent.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeBoostedPoints + BONUS_POINTS, boostedPlayer.getPoints());
		assertEquals(beforeOpponentPoints + MALUS_POINTS, opponent.getPoints());
	}

	// Verifies that ShamanicStarsBC can turn the outcome into a tie at the maximum stars.
	@Test
	void shamanicStarsBuildingCanTurnOutcomeIntoTie() {
		Player boostedPlayer = new Player("boosted");
		Player opponent = playerWithStars("opponent", 3);
		boostedPlayer.addPoints(9);
		opponent.addPoints(1);

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
		int beforeBoostedPoints = boostedPlayer.getPoints();
		int beforeOpponentPoints = opponent.getPoints();
		shamanicRitual.applyEvent(match);

		assertEquals(beforeBoostedPoints + BONUS_POINTS + MALUS_POINTS, boostedPlayer.getPoints());
		assertEquals(beforeOpponentPoints + BONUS_POINTS + MALUS_POINTS, opponent.getPoints());
	}

	// Verifies full tie behavior for 2..5 players: everyone should receive the bonus.
	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4, 5})
	void applyEventAwardsBonusToAllPlayersWhenEveryoneIsTiedFrom2To5Players(int playersCount) {
		Player[] players = new Player[playersCount];
		int[] beforePoints = new int[playersCount];

		for (int i = 0; i < playersCount; i++) {
			players[i] = playerWithStars("player_" + playersCount + "_" + i, 2);
			players[i].addPoints(i + 1);
			beforePoints[i] = players[i].getPoints();
		}

		Match match = createMatch(players);
		shamanicRitual.applyEvent(match);

		for (int i = 0; i < playersCount; i++) {
			assertEquals(beforePoints[i] + BONUS_POINTS + MALUS_POINTS, players[i].getPoints());
		}
	}

	// Verifies the requested flow for full tie: bonus is applied first, then Malus to the same players.
	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4, 5})
	void applyEventAddsBonusAndThenMalusWhenEveryoneHasSameShamanStars(int playersCount) {
		Player[] players = new Player[playersCount];
		int[] beforePoints = new int[playersCount];

		for (int i = 0; i < playersCount; i++) {
			players[i] = playerWithStars("sameStars_" + playersCount + "_" + i, 2);
			players[i].addPoints(30);
			beforePoints[i] = players[i].getPoints();
		}

		Match match = createMatch(players);
		shamanicRitual.applyEvent(match);

		for (int i = 0; i < playersCount; i++) {
			assertEquals(beforePoints[i] + BONUS_POINTS + MALUS_POINTS, players[i].getPoints());
		}
	}

	// Verifies the requested scenario: first player is boosted to win, all tied lowest players get Malus.
	@ParameterizedTest
	@ValueSource(ints = {3, 4, 5})
	void shamanicStarsOwnerWinsBonusAndAllTiedLowestPlayersLosePoints(int playersCount) {
		Player[] players = new Player[playersCount];
		int[] beforePoints = new int[playersCount];

		players[0] = playerWithStars("boosted_" + playersCount, 1);
		for (int i = 1; i < playersCount; i++) {
			players[i] = playerWithStars("lowTied_" + playersCount + "_" + i, 1);
		}

		for (int i = 0; i < playersCount; i++) {
			players[i].addPoints(20 + i);
			beforePoints[i] = players[i].getPoints();
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

		assertEquals(beforePoints[0] + BONUS_POINTS, players[0].getPoints());
		for (int i = 1; i < playersCount; i++) {
			assertEquals(beforePoints[i] + MALUS_POINTS, players[i].getPoints());
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