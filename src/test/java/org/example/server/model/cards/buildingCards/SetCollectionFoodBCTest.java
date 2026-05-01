package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.buildingCards.SetCollectionFoodBC;
import org.example.server.model.cards.characters.Artist;
import org.example.server.model.cards.characters.Builder;
import org.example.server.model.cards.characters.Gatherer;
import org.example.server.model.cards.characters.Hunter;
import org.example.server.model.cards.characters.Inventor;
import org.example.server.model.cards.characters.Shaman;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.InventionType;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetCollectionFoodBCTest {

    @Test
    @DisplayName("correct string")
    void correctString() {
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.SetCollectionFoodBC,
                false
        );
        assertTrue(card.toString().endsWith("\tEffect: get +5 food each time you complete a new character set\n"));
    }
    //Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_initializesAllFieldsCorrectly() {
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.SetCollectionFoodBC, card.getClassType());
    }

    //Test that the first activation only registers already completed sets
    //and does not reward food for sets owned before obtaining the building.
    @Test
    void applyEffect_firstActivationWithExistingSet_doesNotRewardPastSets() {
        Player owner = new Player("owner");
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                1,
                Era.I,
                3,
                6,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        addOneCompleteSet(owner);
        int beforeFood = owner.getFood();

        //Match is not used by this implementation.
        card.applyEffect(owner, null);

        assertEquals(beforeFood, owner.getFood());
    }

    //Test that one newly completed set after initialization gives 5 food.
    @Test
    void applyEffect_afterInitializationWhenOneNewSetIsFormed_addsFiveFood() {
        Player owner = new Player("owner");
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                2,
                Era.I,
                3,
                6,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        // First activation with zero complete sets.
        card.applyEffect(owner, null);

        addOneCompleteSet(owner);
        int beforeFood = owner.getFood();

        card.applyEffect(owner, null);

        assertEquals(beforeFood + 5, owner.getFood());
    }

    //Test that two newly completed sets after initialization give 10 food.
    @Test
    void applyEffect_afterInitializationWhenTwoNewSetsAreFormed_addsTenFood() {
        Player owner = new Player("owner");
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                3,
                Era.I,
                4,
                8,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        //First activation with zero complete sets.
        card.applyEffect(owner, null);

        addOneCompleteSet(owner);
        addOneCompleteSet(owner);
        int beforeFood = owner.getFood();

        card.applyEffect(owner, null);

        assertEquals(beforeFood + 10, owner.getFood());
    }

    //Test that no food is awarded if no additional complete set
    //is formed after the building has already been initialized.
    @Test
    void applyEffect_afterInitializationWithNoNewSets_addsNoFood() {
        Player owner = new Player("owner");
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                4,
                Era.I,
                2,
                5,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        addOneCompleteSet(owner);

        //First activation registers the existing completed set.
        card.applyEffect(owner, null);

        int beforeFood = owner.getFood();

        //No new characters are added, so no new completed set exists.
        card.applyEffect(owner, null);

        assertEquals(beforeFood, owner.getFood());
    }

    //Test that adding this building to a player
    //correctly stores it in the owned buildings list.
    @Test
    void addBuilding_setCollectionFoodCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        SetCollectionFoodBC card = new SetCollectionFoodBC(
                5,
                Era.I,
                3,
                6,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }

    private void addOneCompleteSet(Player owner) {
        int baseId = owner.getInventors().size()
                + owner.getGatherers().size()
                + owner.getShamans().size()
                + owner.getBuilders().size()
                + owner.getArtists().size()
                + owner.getHunters().size()
                + 1000;

        owner.addCharacter(new Inventor(baseId + 1, Era.I, CharacterType.INVENTOR, InventionType.BOAT));
        owner.addCharacter(new Gatherer(baseId + 2, Era.I, CharacterType.GATHERER));
        owner.addCharacter(new Shaman(baseId + 3, Era.I, CharacterType.SHAMAN, 1));
        owner.addCharacter(new Builder(baseId + 4, Era.I, CharacterType.BUILDER, -1, 0));
        owner.addCharacter(new Artist(baseId + 5, Era.I, CharacterType.ARTIST));
        owner.addCharacter(new Hunter(baseId + 6, Era.I, CharacterType.HUNTER, false));
    }
}