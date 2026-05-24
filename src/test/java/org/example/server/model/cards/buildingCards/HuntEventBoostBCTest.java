package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.characters.Hunter;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntEventBoostBCTest {

    @Test
    @DisplayName("Test that with 0 Hunters, points and food remain unchanged")
    void testZeroHunters() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial food and points to 5
        player.addPoints(-player.getPoints() + 5);
        player.addFood(-player.getFood() + 5);

        HuntEventBoostBC card = new HuntEventBoostBC(1, Era.III, 10, 25, BuildingCardType.HuntEventBoostBC, false);
        player.addBuilding(card);

        // No hunters added
        card.applyEffect(player, match);

        // Verify that the values remained at 5
        assertEquals(5, player.getPoints());
        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("Test that with 1 Hunter, it adds 1 point and 1 food")
    void testOneHunter() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial food and points to 5
        player.addPoints(-player.getPoints() + 5);
        player.addFood(-player.getFood() + 5);

        HuntEventBoostBC card = new HuntEventBoostBC(1, Era.III, 10, 25, BuildingCardType.HuntEventBoostBC, false);
        player.addBuilding(card);

        // Hunter from JSON: id 0, Era I, obtainFood true
        Hunter hunter = new Hunter(0, Era.I, CharacterType.HUNTER, true);
        player.addCharacter(hunter);

        card.applyEffect(player, match);

        // 5 base + 1 = 6
        assertEquals(6, player.getPoints());
        assertEquals(6, player.getFood());
    }

    @Test
    @DisplayName("Test that HuntEventBoostBC adds 5 points and 5 food to the player for 5 hunters they own")
    void testAdd5Hunters() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial food and points to 5
        player.addPoints(-player.getPoints() + 5);
        player.addFood(-player.getFood() + 5);

        HuntEventBoostBC card = new HuntEventBoostBC(1, Era.III, 10, 25, BuildingCardType.HuntEventBoostBC, false);
        player.addBuilding(card);

        // Creation of 5 hunters based on the data from your JSON
        Hunter hunter1 = new Hunter(0, Era.I, CharacterType.HUNTER, true);
        Hunter hunter2 = new Hunter(2, Era.I, CharacterType.HUNTER, false);
        Hunter hunter3 = new Hunter(17, Era.II, CharacterType.HUNTER, false);
        Hunter hunter4 = new Hunter(19, Era.II, CharacterType.HUNTER, true);
        Hunter hunter5 = new Hunter(34, Era.III, CharacterType.HUNTER, false);

        // Adding the hunters to the player
        player.addCharacter(hunter1);
        player.addCharacter(hunter2);
        player.addCharacter(hunter3);
        player.addCharacter(hunter4);
        player.addCharacter(hunter5);

        card.applyEffect(player, match);

        // 5 (initial value) + 5 (hunter bonus) = 10
        assertEquals(10, player.getPoints());
        assertEquals(10, player.getFood());
    }

    @Test
    @DisplayName("correct string")
    void correctString() {HuntEventBoostBC card = new HuntEventBoostBC(1, Era.III, 10, 25, BuildingCardType.HuntEventBoostBC, false);
        assertTrue(card.toString().endsWith("\tEffect: during Hunt Event, for each  Hunter in your tribe get +1 points and +1 food\n"));
    }
}