package org.example.model.cards.buildingCards;

import org.example.model.cards.characters.Builder;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndGameBonusBCTest {

    @Test
    @DisplayName("Test that with 0 Builders, points remain unchanged")
    void testZeroBuilders() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial points to 5
        player.addPoints(-player.getPoints() + 5);

        EndGameBonusBC card = new EndGameBonusBC(121, Era.III, 10, 0, BuildingCardType.EndGameBonusBC, true);
        player.addBuilding(card);

        // No builders added
        card.applyEffect(player, match);

        // Verify that points remained at 5 (5 + 0 bonus)
        assertEquals(5, player.getPoints());
    }

    @Test
    @DisplayName("Test that with 1 Builder, it adds the correct amount of endPoints")
    void testOneBuilder() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial points to 5
        player.addPoints(-player.getPoints() + 5);

        EndGameBonusBC card = new EndGameBonusBC(121, Era.III, 10, 0, BuildingCardType.EndGameBonusBC, true);
        player.addBuilding(card);

        // Builder from JSON: id 3, Era I, discountBuilding -1, endPoints 3
        Builder builder = new Builder(3, Era.I, CharacterType.BUILDER, -1, 3);
        player.addCharacter(builder);

        card.applyEffect(player, match);

        // 5 base + 3 (from builder) = 8
        assertEquals(8, player.getPoints());
    }

    @Test
    @DisplayName("Test that a Builder with 0 endPoints adds nothing to the player score")
    void testBuilderWithZeroPoints() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial points to 5
        player.addPoints(-player.getPoints() + 5);

        EndGameBonusBC card = new EndGameBonusBC(121, Era.III, 10, 0, BuildingCardType.EndGameBonusBC, true);
        player.addBuilding(card);

        // Builder from JSON: id 4, Era I, discountBuilding -2, endPoints 0
        Builder builderWithZeroPoints = new Builder(4, Era.I, CharacterType.BUILDER, -2, 0);
        player.addCharacter(builderWithZeroPoints);

        card.applyEffect(player, match);

        // 5 base + 0 = 5
        assertEquals(5, player.getPoints());
    }

    @Test
    @DisplayName("Test that multiple Builders sum up their endPoints correctly")
    void testMultipleBuilders() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);

        // Set initial points to 5
        player.addPoints(-player.getPoints() + 5);

        EndGameBonusBC card = new EndGameBonusBC(121, Era.III, 10, 0, BuildingCardType.EndGameBonusBC, true);
        player.addBuilding(card);

        // Creation of multiple builders based on the data from your JSON
        // id 5: 2 points
        Builder builder1 = new Builder(5, Era.I, CharacterType.BUILDER, -1, 2);
        // id 20: 4 points
        Builder builder2 = new Builder(20, Era.II, CharacterType.BUILDER, -1, 4);
        // id 37: 5 points
        Builder builder3 = new Builder(37, Era.III, CharacterType.BUILDER, -1, 5);
        // id 38: 3 points
        Builder builder4 = new Builder(38, Era.III, CharacterType.BUILDER, -2, 3);

        // Adding the builders to the player
        player.addCharacter(builder1);
        player.addCharacter(builder2);
        player.addCharacter(builder3);
        player.addCharacter(builder4);

        card.applyEffect(player, match);

        // Total bonus from builders: 2 + 4 + 5 + 3 = 14
        // 5 (initial value) + 14 (bonus) = 19
        assertEquals(19, player.getPoints());
    }
}