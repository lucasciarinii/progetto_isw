package it.polimi.ingsw.server.model.cards.buildingCards;

import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundFlowTotemBCTest {

    //Stub Match used only to provide a valid Match instance
    //without relying on additional game behavior.
    private static class DummyMatch extends Match {
        DummyMatch() {
            super(java.util.List.of(new Player("p1"), new Player("p2")));
        }
    }

    //Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_initializesAllFieldsCorrectly() {
        RoundFlowTotemBC card = new RoundFlowTotemBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.RoundFlowTotemBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.RoundFlowTotemBC, card.getClassType());
    }

    //Test that applyEffect adds exactly one food to the owner.
    @Test
    void applyEffect_validOwnerAndMatch_addsExactlyOneFoodToOwner() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowTotemBC card = new RoundFlowTotemBC(
                1,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowTotemBC,
                false
        );

        int beforeFood = owner.getFood();

        card.applyEffect(owner, match);

        assertEquals(beforeFood + 1, owner.getFood());
    }

    //Test that applying the effect multiple times adds food cumulatively.
    @Test
    void applyEffect_calledTwice_addsFoodCumulatively() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowTotemBC card = new RoundFlowTotemBC(
                2,
                Era.I,
                2,
                4,
                BuildingCardType.RoundFlowTotemBC,
                false
        );

        int beforeFood = owner.getFood();

        card.applyEffect(owner, match);
        card.applyEffect(owner, match);

        assertEquals(beforeFood + 2, owner.getFood());
    }

    //Test that applyEffect changes only food
    //and does not modify the other observable owner state.
    @Test
    void applyEffect_changesOnlyFood_notOtherOwnerState() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowTotemBC card = new RoundFlowTotemBC(
                3,
                Era.I,
                1,
                6,
                BuildingCardType.RoundFlowTotemBC,
                false
        );

        int beforePoints = owner.getPoints();
        int beforeFood = owner.getFood();
        int beforeSustenanceDiscount = owner.getDiscountOnSustenance();
        int beforeBuildingDiscount = owner.getDiscountOnBuilding();
        int beforeShamanStars = owner.getShamanStars();

        int beforeOwnedBuildings = owner.getOwnedBuildings().size();
        int beforeInventors = owner.getInventors().size();
        int beforeGatherers = owner.getGatherers().size();
        int beforeShamans = owner.getShamans().size();
        int beforeBuilders = owner.getBuilders().size();
        int beforeArtists = owner.getArtists().size();
        int beforeHunters = owner.getHunters().size();

        card.applyEffect(owner, match);

        assertEquals(beforePoints, owner.getPoints());
        assertEquals(beforeFood + 1, owner.getFood());
        assertEquals(beforeSustenanceDiscount, owner.getDiscountOnSustenance());
        assertEquals(beforeBuildingDiscount, owner.getDiscountOnBuilding());
        assertEquals(beforeShamanStars, owner.getShamanStars());

        assertEquals(beforeOwnedBuildings, owner.getOwnedBuildings().size());
        assertEquals(beforeInventors, owner.getInventors().size());
        assertEquals(beforeGatherers, owner.getGatherers().size());
        assertEquals(beforeShamans, owner.getShamans().size());
        assertEquals(beforeBuilders, owner.getBuilders().size());
        assertEquals(beforeArtists, owner.getArtists().size());
        assertEquals(beforeHunters, owner.getHunters().size());
    }

    //Test that adding a RoundFlowTotemBC to a player
    //correctly stores it in the owned buildings list.
    @Test
    void addBuilding_roundFlowTotemCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        RoundFlowTotemBC card = new RoundFlowTotemBC(
                4,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowTotemBC,
                false
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }

    @Test
    @DisplayName("correct string")
    void correctString() { RoundFlowTotemBC card = new RoundFlowTotemBC(
            4,
            Era.I,
            3,
            7,
            BuildingCardType.RoundFlowTotemBC,
            false);
        assertTrue(card.toString().endsWith("\tEffect: get +1 food if you return on a bonus tile\n"));
    }
}