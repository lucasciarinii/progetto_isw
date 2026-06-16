package it.polimi.ingsw.server.model.cards.buildingCards;

import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanicDoublePointsBCTest {

    // Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_shouldInitializeAllInheritedFieldsCorrectly() {
        ShamanicDoublePointsBC card = new ShamanicDoublePointsBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.ShamanicDoublePointsBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.ShamanicDoublePointsBC, card.getClassType());
    }

    // Test that applyEffect does not throw exceptions when called with valid arguments.
    @Test
    void applyEffect_shouldNotThrowException_whenOwnerIsValid() {
        Player owner = new Player("owner");
        ShamanicDoublePointsBC card = new ShamanicDoublePointsBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicDoublePointsBC,
                false
        );

        assertDoesNotThrow(() -> card.applyEffect(owner, null));
    }
    @Test
    @DisplayName("correct string")
    void correctString() {
        ShamanicDoublePointsBC card = new ShamanicDoublePointsBC(1, Era.I, 3, 6, BuildingCardType.ShamanicDoublePointsBC, false);
        assertTrue(card.toString().endsWith("\tEffect: during shamanic ritual if you have the most stars get X2 of indicated points\n"));
    }
}
