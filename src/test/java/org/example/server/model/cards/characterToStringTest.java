package org.example.server.model.cards; // Ensure this matches your test package path

import org.example.client.view.tui.ConsoleColors;
import org.example.server.model.cards.characters.*;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.InventionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class to verify the toString() method for all Character subclasses.
 */
class CharacterToStringTest {

    @Test
    @DisplayName("Test Artist toString() output")
    void testArtistToString() {
        Artist artist = new Artist(1, Era.I, CharacterType.ARTIST);

        String result = artist.toString();

        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.ARTIST, 1, Era.I);
        String expectedFullString = String.format("%s%s%s", ConsoleColors.YELLOW, expectedSuperString, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Artist is incorrect.");
    }

    @Test
    @DisplayName("Test Builder toString() output")
    void testBuilderToString() {
        // Arrange: Setup data for the Builder
        int discount = 2;
        int endPoints = 5;
        Builder builder = new Builder(2, Era.II, CharacterType.BUILDER, discount, endPoints);

        // Act
        String result = builder.toString();

        // Assert
        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.BUILDER, 2, Era.II);
        String expectedFullString = String.format("%s%s\tdiscount: %d\n\tendPoints: %d%s\n",
                ConsoleColors.GREY, expectedSuperString, discount, endPoints, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Builder is incorrect.");
    }

    @Test
    @DisplayName("Test Gatherer toString() output")
    void testGathererToString() {
        // Arrange
        Gatherer gatherer = new Gatherer(3, Era.III, CharacterType.GATHERER);

        // Act
        String result = gatherer.toString();

        // Assert
        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.GATHERER, 3, Era.III);
        String expectedFullString = String.format("%s%s%s", ConsoleColors.ORANGE, expectedSuperString, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Gatherer is incorrect.");
    }

    @Test
    @DisplayName("Test Hunter toString() output when obtainFood is true")
    void testHunterToStringTrue() {
        // Arrange
        Hunter hunter = new Hunter(4, Era.I, CharacterType.HUNTER, true);

        // Act
        String result = hunter.toString();

        // Assert
        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.HUNTER, 4, Era.I);
        String expectedFullString = String.format("%s%s\tobtainFood: YES%s\n",
                ConsoleColors.RED, expectedSuperString, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Hunter (true) is incorrect.");
    }

    @Test
    @DisplayName("Test Inventor toString() output")
    void testInventorToString() {
        // Arrange: Create an Inventor providing a valid InventionType enum
        Inventor inventor = new Inventor(5, Era.II, CharacterType.INVENTOR, InventionType.BOAT);

        // Act
        String result = inventor.toString();

        // Assert
        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.INVENTOR, 5, Era.II);
        String expectedFullString = String.format("%s%s\tinvention: %s%s\n",
                ConsoleColors.MINT, expectedSuperString, InventionType.BOAT, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Inventor is incorrect.");
    }

    @Test
    @DisplayName("Test Shaman toString() output")
    void testShamanToString() {
        // Arrange
        int numStars = 3;
        Shaman shaman = new Shaman(6, Era.III, CharacterType.SHAMAN, numStars);

        // Act
        String result = shaman.toString();

        // Assert
        String expectedSuperString = String.format("%s [id: %d] {ERA %s}\n", CharacterType.SHAMAN, 6, Era.III);
        String expectedFullString = String.format("%s%s\tstars: %d%s\n",
                ConsoleColors.PURPLE, expectedSuperString, numStars, ConsoleColors.RESET);

        assertEquals(expectedFullString, result, "The toString output for Shaman is incorrect.");
    }
}