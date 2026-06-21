package it.polimi.ingsw.server.model.cards.buildingCards;

import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanicStarsBCTest {

    // Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_shouldInitializeAllInheritedFieldsCorrectly() {
        ShamanicStarsBC card = new ShamanicStarsBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.ShamanicStarsBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.ShamanicStarsBC, card.getClassType());
    }

    // Test that applyEffect adds exactly 3 shaman stars to the owner.
    @Test
    void applyEffect_shouldAddThreeShamanStars_toOwner() {
        Player owner = new Player("owner");
        ShamanicStarsBC card = new ShamanicStarsBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicStarsBC,
                false
        );

        int beforeShamanStars = owner.getShamanStars();
        card.applyEffect(owner, null);

        assertEquals(beforeShamanStars + 3, owner.getShamanStars());
    }

    // Test that applyEffect does not throw exceptions when called with valid arguments.
    @Test
    void applyEffect_shouldNotThrowException_whenOwnerIsValid() {
        Player owner = new Player("owner");
        ShamanicStarsBC card = new ShamanicStarsBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicStarsBC,
                false
        );

        assertDoesNotThrow(() -> card.applyEffect(owner, null));
    }

    // Test that applyEffect only modifies shaman stars and does not change other owner state.
    @Test
    void applyEffect_shouldOnlyModifyShamanStars_andNotChangeOtherState() {
        Player owner = new Player("owner");
        ShamanicStarsBC card = new ShamanicStarsBC(
                2,
                Era.I,
                3,
                6,
                BuildingCardType.ShamanicStarsBC,
                false
        );

        int beforePoints = owner.getPoints();
        int beforeFood = owner.getFood();
        int beforeDiscountOnSustenance = owner.getDiscountOnSustenance();
        int beforeDiscountOnBuilding = owner.getDiscountOnBuilding();
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
        assertEquals(beforeOwnedBuildingsSize, owner.getOwnedBuildings().size());
        assertEquals(beforeInventorsSize, owner.getInventors().size());
        assertEquals(beforeGatherersSize, owner.getGatherers().size());
        assertEquals(beforeShamansSize, owner.getShamans().size());
        assertEquals(beforeBuildersSize, owner.getBuilders().size());
        assertEquals(beforeArtistsSize, owner.getArtists().size());
        assertEquals(beforeHuntersSize, owner.getHunters().size());
    }

    // Test that adding this building to a player stores it in the owned buildings list.
    @Test
    void addBuilding_shamanicStarsCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        ShamanicStarsBC card = new ShamanicStarsBC(
                3,
                Era.III,
                4,
                8,
                BuildingCardType.ShamanicStarsBC,
                false
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }

    @Test
    @DisplayName("correct string")
    void correctString() {ShamanicStarsBC card = new ShamanicStarsBC(
            3,
            Era.III,
            4,
            8,
            BuildingCardType.ShamanicStarsBC,
            false
    );
        assertTrue(card.toString().endsWith("\tEffect: during shamanic ritual get +3 stars\n"));
    }
}