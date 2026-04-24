package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.buildingCards.EndGameBonus25BC;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndGameBonus25BCTest {
    @Test
    @DisplayName("Test that EndGameBonus25BC adds 25 points to the player")
    void testAdd25_pointsStartsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addPoints(-player.getPoints() + 5);

        EndGameBonus25BC card = new EndGameBonus25BC(120,Era.III,10,25,BuildingCardType.EndGameBonus25BC,true);

        player.addBuilding(card);
        card.applyEffect(player, match);

        assertEquals(30, player.getPoints());
    }

    @Test
    @DisplayName("Test that EndGameBonus25BC adds 25 points to the player")
    void testAdd25_pointsStartNegative() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addPoints(-player.getPoints() - 25);

        EndGameBonus25BC card = new EndGameBonus25BC(120,Era.III,10,25,BuildingCardType.EndGameBonus25BC,true);

        player.addBuilding(card);
        card.applyEffect(player, match);

        assertEquals(0, player.getPoints());
    }

    @Test
    @DisplayName("Test that EndGameBonus25BC.toString returns the correct string")
    void testToString() {
        EndGameBonus25BC card = new EndGameBonus25BC(120, Era.III, 10, 25, BuildingCardType.EndGameBonus25BC, true);
        assertTrue(card.toString().endsWith("\tEffect: get 25 points (end game)\n"));
    }
}
