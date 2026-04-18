package org.example.model.cards.buildingCards;

import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanicNoMalusBCTest {

    //Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_shouldInitializeAllInheritedFieldsCorrectly() {
        ShamanicNoMalusBC card = new ShamanicNoMalusBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.ShamanicNoMalusBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.ShamanicNoMalusBC, card.getClassType());
    }

    //Test that applyEffect does not throw exceptions when called with valid arguments.
    @Test
    void applyEffect_shouldNotThrowException_whenOwnerIsValid() {
        Player owner = new Player("owner");
        ShamanicNoMalusBC card = new ShamanicNoMalusBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicNoMalusBC,
                false
        );

        assertDoesNotThrow(() -> card.applyEffect(owner, null));
    }

    //Test that applyEffect does not change the owner state because the method is empty.
    @Test
    void applyEffect_shouldNotChangeOwnerState_whenMethodIsEmpty() {
        Player owner = new Player("owner");
        ShamanicNoMalusBC card = new ShamanicNoMalusBC(
                2,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicNoMalusBC,
                false
        );

        int beforePoints = owner.getPoints();
        int beforeFood = owner.getFood();
        int beforeDiscountOnSustenance = owner.getDiscountOnSustenance();
        int beforeDiscountOnBuilding = owner.getDiscountOnBuilding();
        int beforeShamanStars = owner.getShamanStars();
        int beforeOwnedBuildingsSize = owner.getOwnedBuildings().size();
        int beforeInventorsSize = owner.getInventors().size();
        int beforeGatherersSize = owner.getGatherers().size();
        int beforeShamansSize = owner.getShamans().size();
        int beforeBuildersSize = owner.getBuilders().size();
        int beforeArtistsSize = owner.getArtists().size();
        int beforeHuntersSize = owner.getHunters().size();

        card.applyEffect(owner, null);

        assertEquals(beforePoints, owner.getPoints());
        assertEquals(beforeFood, owner.getFood());
        assertEquals(beforeDiscountOnSustenance, owner.getDiscountOnSustenance());
        assertEquals(beforeDiscountOnBuilding, owner.getDiscountOnBuilding());
        assertEquals(beforeShamanStars, owner.getShamanStars());
        assertEquals(beforeOwnedBuildingsSize, owner.getOwnedBuildings().size());
        assertEquals(beforeInventorsSize, owner.getInventors().size());
        assertEquals(beforeGatherersSize, owner.getGatherers().size());
        assertEquals(beforeShamansSize, owner.getShamans().size());
        assertEquals(beforeBuildersSize, owner.getBuilders().size());
        assertEquals(beforeArtistsSize, owner.getArtists().size());
        assertEquals(beforeHuntersSize, owner.getHunters().size());
    }

    //Test that adding this building to a player stores it in the owned buildings list.
    @Test
    void addBuilding_shamanicNoMalusCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        ShamanicNoMalusBC card = new ShamanicNoMalusBC(
                3,
                Era.III,
                4,
                8,
                BuildingCardType.ShamanicNoMalusBC,
                false
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }
}