package org.example.server.model.cards.buildingCards;

import org.example.server.model.cards.buildingCards.SetCollectionEndPointsBC;
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

class SetCollectionEndPointsBCTest {

    //Test that the constructor correctly initializes all inherited fields.
    @Test
    void constructor_initializesAllFieldsCorrectly() {
        SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
                42,
                Era.II,
                5,
                12,
                BuildingCardType.SetCollectionEndPointsBC,
                true
        );

        assertEquals(42, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(5, card.getFoodCost());
        assertEquals(12, card.getEndPoints());
        assertEquals(BuildingCardType.SetCollectionEndPointsBC, card.getClassType());
    }

    @Test
    @DisplayName("correct string")
    void correctString() { SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
            1,
            Era.III,
            3,
            6,
            BuildingCardType.SetCollectionEndPointsBC,
            true
    );
        assertTrue(card.toString().endsWith("\tEffect: get +6 points for each completed set of characters (end game)\n"));
    }

    //Test that no complete set gives no additional points.
    @Test
    void applyEffect_noCompleteSets_addsNoPoints() {
        Player owner = new Player("owner");
        SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
                1,
                Era.III,
                3,
                6,
                BuildingCardType.SetCollectionEndPointsBC,
                true
        );

        int beforePoints = owner.getPoints();

        owner.addCharacter(createInventor(1001));
        owner.addCharacter(createGatherer(1002));
        owner.addCharacter(createShaman(1003));
        owner.addCharacter(createBuilder(1004));
        owner.addCharacter(createArtist(1005));
        //No hunter added, so there is no complete set of six different types.

        //Match is not used by this implementation.
        card.applyEffect(owner, null);

        assertEquals(beforePoints, owner.getPoints());
    }

    //Test that one complete set of six different character types gives 6 points.
    @Test
    void applyEffect_oneCompleteSet_addsSixPoints() {
        Player owner = new Player("owner");
        SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
                2,
                Era.III,
                3,
                6,
                BuildingCardType.SetCollectionEndPointsBC,
                true
        );

        int beforePoints = owner.getPoints();

        owner.addCharacter(createInventor(2001));
        owner.addCharacter(createGatherer(2002));
        owner.addCharacter(createShaman(2003));
        owner.addCharacter(createBuilder(2004));
        owner.addCharacter(createArtist(2005));
        owner.addCharacter(createHunter(2006));

        card.applyEffect(owner, null);

        assertEquals(beforePoints + 6, owner.getPoints());
    }

    //Test that the number of completed sets is determined
    //by the minimum count among the six character-type lists.
    @Test
    void applyEffect_usesMinimumCharacterTypeCount_toDetermineCompletedSets() {
        Player owner = new Player("owner");
        SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
                3,
                Era.III,
                4,
                8,
                BuildingCardType.SetCollectionEndPointsBC,
                true
        );

        int beforePoints = owner.getPoints();

        owner.addCharacter(createInventor(3001));
        owner.addCharacter(createInventor(3002));
        owner.addCharacter(createInventor(3003));

        owner.addCharacter(createGatherer(3004));
        owner.addCharacter(createGatherer(3005));

        owner.addCharacter(createShaman(3006));
        owner.addCharacter(createShaman(3007));
        owner.addCharacter(createShaman(3008));
        owner.addCharacter(createShaman(3009));

        owner.addCharacter(createBuilder(3010));
        owner.addCharacter(createBuilder(3011));

        owner.addCharacter(createArtist(3012));
        owner.addCharacter(createArtist(3013));
        owner.addCharacter(createArtist(3014));
        owner.addCharacter(createArtist(3015));
        owner.addCharacter(createArtist(3016));

        owner.addCharacter(createHunter(3017));
        owner.addCharacter(createHunter(3018));

        //Minimum count is 2, so the player has 2 complete sets.
        //Each set gives 6 points, so expected bonus is 12.
        card.applyEffect(owner, null);

        assertEquals(beforePoints + 12, owner.getPoints());
    }

    //Test that adding this building to a player
    //correctly stores it in the owned buildings list.
    @Test
    void addBuilding_setCollectionEndPointsCard_isStoredInOwnedBuildings() {
        Player player = new Player("owner");
        SetCollectionEndPointsBC card = new SetCollectionEndPointsBC(
                4,
                Era.III,
                3,
                6,
                BuildingCardType.SetCollectionEndPointsBC,
                true
        );

        player.addBuilding(card);

        assertEquals(1, player.getOwnedBuildings().size());
        assertTrue(player.getOwnedBuildings().contains(card));
    }

    private Inventor createInventor(int id) {
        return new Inventor(id, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
    }

    private Gatherer createGatherer(int id) {
        return new Gatherer(id, Era.I, CharacterType.GATHERER);
    }

    private Shaman createShaman(int id) {
        return new Shaman(id, Era.I, CharacterType.SHAMAN, 1);
    }

    private Builder createBuilder(int id) {
        return new Builder(id, Era.I, CharacterType.BUILDER, -2, 0);
    }

    private Artist createArtist(int id) {
        return new Artist(id, Era.I, CharacterType.ARTIST);
    }

    private Hunter createHunter(int id) {
        return new Hunter(id, Era.I, CharacterType.HUNTER, false);
    }
}