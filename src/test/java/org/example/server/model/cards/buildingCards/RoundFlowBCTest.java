package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.buildingCards.RoundFlowBC;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundFlowBCTest {

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
        RoundFlowBC card = new RoundFlowBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.RoundFlowBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.RoundFlowBC, card.getClassType());
    }

    //Test that applyEffect does not throw exceptions with valid parameters.
    @Test
    void applyEffect_validOwnerAndMatch_doesNotThrow() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowBC card = new RoundFlowBC(
                1,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowBC,
                false
        );

        assertDoesNotThrow(() -> card.applyEffect(owner, match));
    }

    //Test that the current empty implementation of applyEffect
    //does not modify the owner's observable state.
    @Test
    void applyEffect_emptyImplementation_doesNotChangeOwnerState() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowBC card = new RoundFlowBC(
                1,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowBC,
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
        assertEquals(beforeFood, owner.getFood());
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

    //Test that the current empty implementation of applyEffect
    //does not alter the observable Match state.
    @Test
    void applyEffect_emptyImplementation_doesNotChangeMatchState() {
        Player owner = new Player("owner");
        Match match = new DummyMatch();
        RoundFlowBC card = new RoundFlowBC(
                1,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowBC,
                false
        );

        Object boardBefore = match.getBoard();
        Object gameStateBefore = match.getGameState();
        int playersBefore = match.getPlayers().size();

        card.applyEffect(owner, match);

        assertSame(boardBefore, match.getBoard());
        assertSame(gameStateBefore, match.getGameState());
        assertEquals(playersBefore, match.getPlayers().size());
    }

    //Test that adding a RoundFlowBC to a player
    //correctly stores it in the owned buildings list.
    @Test
    void addBuilding_roundFlowCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        RoundFlowBC card = new RoundFlowBC(
                1,
                Era.I,
                3,
                7,
                BuildingCardType.RoundFlowBC,
                false
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }
}