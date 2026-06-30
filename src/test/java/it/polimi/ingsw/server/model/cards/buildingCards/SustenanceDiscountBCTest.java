package it.polimi.ingsw.server.model.cards.buildingCards;

import it.polimi.ingsw.server.model.cards.characters.*;
import it.polimi.ingsw.server.model.cards.characters.*;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.InventionType;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SustenanceDiscountBCTest {


    // Test that the constructor correctly initializes all inherited fields and the characterEffect field
    @Test
    void constructor_initializesAllFieldsCorrectly() {
        SustenanceDiscountBC card = new SustenanceDiscountBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.SustenanceDiscountBC,
                CharacterType.INVENTOR,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.SustenanceDiscountBC, card.getClassType());
    }

    @Test
    @DisplayName("correct string")
    void correctString() { SustenanceDiscountBC card = new SustenanceDiscountBC(
            10,
            Era.I,
            3,
            6,
            BuildingCardType.SustenanceDiscountBC,
            CharacterType.INVENTOR,
            false
    );
        assertTrue(card.toString().endsWith("\tEffect: during sustenance get a -1 food discount for each INVENTOR in your tribe\n"));
    }

    // Parametrized test that verifies applyEffect correctly computes discount for supported character types
    // Tests verify that the discount equals the number of characters owned by the player for each supported type
    @ParameterizedTest(name = "Character Type: {0}, Count: {1}, Expected Discount: {2}")
    @MethodSource("provideSupportedCharacterCases")
    void applyEffect_withSupportedCharacterType_addsCorrectDiscount(
            CharacterType characterType, int characterCount, int expectedDiscount) {

        Player owner = new Player("testPlayer");
        SustenanceDiscountBC card = new SustenanceDiscountBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.SustenanceDiscountBC,
                characterType,
                false
        );

        // Add the required number of characters of the specified type
        addCharactersToPlayer(owner, characterType, characterCount);

        // Apply the building effect
        card.applyEffect(owner, null);

        // The discount should increase by the expected amount
        assertEquals(expectedDiscount, card.getDiscountOnGame(),
                "Discount should be " + expectedDiscount + " for " + characterCount + " " + characterType);
    }

    // Parametrized test that verifies applyEffect correctly handles unsupported character types
    // Unsupported types should result in zero discount being added
    @ParameterizedTest(name = "Unsupported Character Type: {0}")
    @CsvSource({
            "SHAMAN",
            "HUNTER",
            "BUILDER"
    })
    void applyEffect_withUnsupportedCharacterType_addsNoDiscount(CharacterType unsupportedType) {
        Player owner = new Player("testPlayer");
        SustenanceDiscountBC card = new SustenanceDiscountBC(
                5,
                Era.I,
                2,
                4,
                BuildingCardType.SustenanceDiscountBC,
                unsupportedType,
                false
        );

        int discountBefore = owner.getDiscountOnSustenance();

        // Apply the building effect with unsupported character type
        card.applyEffect(owner, null);

        // The discount should remain unchanged (default returns 0 for unsupported types)
        assertEquals(discountBefore, owner.getDiscountOnSustenance(),
                "Discount should not change for unsupported character type: " + unsupportedType);
    }

    // Test that verifies applyEffect does not add discount when player has characters of different types
    // than the one the card is looking for, even though the player has many characters
    // This ensures the discount only counts characters matching the card's characterEffect
    @Test
    void applyEffect_playerHasDifferentCharacterType_addsNoDiscount() {
        Player owner = new Player("testPlayer");
        // Card expects INVENTOR characters
        SustenanceDiscountBC card = new SustenanceDiscountBC(
                10,
                Era.I,
                3,
                6,
                BuildingCardType.SustenanceDiscountBC,
                CharacterType.INVENTOR,
                false
        );

        // Add a SHAMAN to the player (unsupported type)
        owner.addCharacter(new Shaman(4001, Era.I, CharacterType.SHAMAN, 2));
        // Add a HUNTER to the player (also unsupported type)
        owner.addCharacter(new Hunter(5001, Era.I, CharacterType.HUNTER, true));

        int discountBefore = owner.getDiscountOnSustenance();

        // Apply the building effect - it looks for INVENTOR but player has SHAMAN and HUNTER
        card.applyEffect(owner, null);

        // The discount should remain unchanged because the card type doesn't match any player's characters
        assertEquals(discountBefore, owner.getDiscountOnSustenance(),
                "Discount should not increase when player has different character types than card expects");
    }


    // Test that building can be added to player's owned buildings list
    // This verifies the building follows the standard visitor pattern for inventory management
    @Test
    void addBuilding_sustenanceDiscountCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        SustenanceDiscountBC card = new SustenanceDiscountBC(
                6,
                Era.II,
                5,
                10,
                BuildingCardType.SustenanceDiscountBC,
                CharacterType.ARTIST,
                true
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }

    // Helper method that adds the specified number of characters of a given type to a player
    private void addCharactersToPlayer(Player owner, CharacterType characterType, int count) {
        for (int i = 0; i < count; i++) {
            switch (characterType) {
                case INVENTOR -> owner.addCharacter(
                        new Inventor(1000 + i, Era.I, CharacterType.INVENTOR, InventionType.BOAT)
                );
                case GATHERER -> owner.addCharacter(
                        new Gatherer(2000 + i, Era.I, CharacterType.GATHERER)
                );
                case ARTIST -> owner.addCharacter(
                        new Artist(3000 + i, Era.I, CharacterType.ARTIST)
                );
                // Other types are not supported by the discount logic
            }
        }
    }

    // Provides test data for the parametrized test testing supported character types
    // Each case specifies: characterType, numberOfCharactersToAdd, expectedDiscount
    static Stream<org.junit.jupiter.params.provider.Arguments> provideSupportedCharacterCases() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.INVENTOR, 3, 3),
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.GATHERER, 2, 2),
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.ARTIST, 4, 4),
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.INVENTOR, 0, 0),
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.GATHERER, 0, 0),
                org.junit.jupiter.params.provider.Arguments.of(CharacterType.ARTIST, 1, 1)
        );
    }

}