package org.example.model.match;

import org.example.model.cards.buildingCards.SetCollectionFoodBC;
import org.example.model.cards.characters.Artist;
import org.example.model.cards.characters.Builder;
import org.example.model.cards.characters.Gatherer;
import org.example.model.cards.characters.Hunter;
import org.example.model.cards.characters.Inventor;
import org.example.model.cards.characters.Shaman;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.enums.InventionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    //Test that the constructor correctly initializes nickname and default values.
    @Test
    void constructor_shouldInitializeNicknameAndDefaultState() {
        Player player = new Player("alice");

        assertEquals("alice", player.getNickname());
        assertEquals(0, player.getPoints());
        assertEquals(0, player.getFood());
        assertEquals(0, player.getDiscountOnSustenance());
        assertEquals(0, player.getDiscountOnBuilding());
        assertEquals(0, player.getShamanStars());
        assertTrue(player.getOwnedBuildings().isEmpty());
        assertTrue(player.getInventors().isEmpty());
        assertTrue(player.getGatherers().isEmpty());
        assertTrue(player.getShamans().isEmpty());
        assertTrue(player.getBuilders().isEmpty());
        assertTrue(player.getArtists().isEmpty());
        assertTrue(player.getHunters().isEmpty());
    }

    //Test that constructor rejects a null nickname.
    @Test
    void constructor_shouldThrowException_whenNicknameIsNull() {
        assertThrows(NullPointerException.class, () -> new Player(null));
    }

    //Test that points are accumulated correctly.
    @Test
    void addPoints_shouldAccumulatePoints() {
        Player player = new Player("alice");

        player.addPoints(4);
        player.addPoints(6);

        assertEquals(10, player.getPoints());
    }

    //Test that food is accumulated correctly.
    @Test
    void addFood_shouldAccumulateFood() {
        Player player = new Player("alice");

        player.addFood(2);
        player.addFood(5);

        assertEquals(7, player.getFood());
    }

    //Test that sustenance discount is accumulated for valid positive values.
    @Test
    void addDiscountOnSustenance_shouldAccumulateDiscount_whenValueIsPositive() {
        Player player = new Player("alice");

        player.addDiscountOnSustenance(3);
        player.addDiscountOnSustenance(6);

        assertEquals(9, player.getDiscountOnSustenance());
    }

    //Test that sustenance discount rejects non-positive values.
    @Test
    void addDiscountOnSustenance_shouldThrowException_whenValueIsNotPositive() {
        Player player = new Player("alice");

        assertThrows(IllegalArgumentException.class, () -> player.addDiscountOnSustenance(0));
        assertThrows(IllegalArgumentException.class, () -> player.addDiscountOnSustenance(-1));
    }

    //Test that building discount is accumulated for valid positive values.
    @Test
    void addDiscountOnBuilding_shouldAccumulateDiscount_whenValueIsPositive() {
        Player player = new Player("alice");

        player.addDiscountOnBuilding(1);
        player.addDiscountOnBuilding(2);

        assertEquals(3, player.getDiscountOnBuilding());
    }

    //Test that building discount rejects non-positive values.
    @Test
    void addDiscountOnBuilding_shouldThrowException_whenValueIsNotPositive() {
        Player player = new Player("alice");

        assertThrows(IllegalArgumentException.class, () -> player.addDiscountOnBuilding(0));
        assertThrows(IllegalArgumentException.class, () -> player.addDiscountOnBuilding(-2));
    }

    //Test that shaman stars are accumulated for valid positive values.
    @Test
    void addShamanStars_shouldAccumulateStars_whenValueIsPositive() {
        Player player = new Player("alice");

        player.addShamanStars(2);
        player.addShamanStars(3);

        assertEquals(5, player.getShamanStars());
    }

    //Test that shaman stars reject non-positive values.
    @Test
    void addShamanStars_shouldThrowException_whenValueIsNotPositive() {
        Player player = new Player("alice");

        assertThrows(IllegalArgumentException.class, () -> player.addShamanStars(0));
        assertThrows(IllegalArgumentException.class, () -> player.addShamanStars(-3));
    }

    //Test that addCharacter rejects null input.
    @Test
    void addCharacter_shouldThrowException_whenCharacterIsNull() {
        Player player = new Player("alice");

        assertThrows(IllegalArgumentException.class, () -> player.addCharacter(null));
    }

    //Test that addCharacter stores each runtime type in the correct list
    //and applies the Gatherer side effect on sustenance discount.
    @Test
    void addCharacter_shouldStoreEachCharacterInCorrectList() {
        Player player = new Player("alice");

        Inventor inventor = createInventor(1);
        Gatherer gatherer = createGatherer(2);
        Shaman shaman = createShaman(3);
        Builder builder = createBuilder(4);
        Artist artist = createArtist(5);
        Hunter hunter = createHunter(6);

        player.addCharacter(inventor);
        player.addCharacter(gatherer);
        player.addCharacter(shaman);
        player.addCharacter(builder);
        player.addCharacter(artist);
        player.addCharacter(hunter);

        assertEquals(1, player.getInventors().size());
        assertSame(inventor, player.getInventors().get(0));

        assertEquals(1, player.getGatherers().size());
        assertSame(gatherer, player.getGatherers().get(0));

        assertEquals(1, player.getShamans().size());
        assertSame(shaman, player.getShamans().get(0));

        assertEquals(1, player.getBuilders().size());
        assertSame(builder, player.getBuilders().get(0));

        assertEquals(1, player.getArtists().size());
        assertSame(artist, player.getArtists().get(0));

        assertEquals(1, player.getHunters().size());
        assertSame(hunter, player.getHunters().get(0));

        assertEquals(3, player.getDiscountOnSustenance());
    }

    //Test that addBuilding rejects null input.
    @Test
    void addBuilding_shouldThrowException_whenBuildingIsNull() {
        Player player = new Player("alice");

        assertThrows(IllegalArgumentException.class, () -> player.addBuilding(null));
    }

    //Test that addBuilding stores the building in ownedBuildings.
    @Test
    void addBuilding_shouldStoreBuildingInOwnedBuildings() {
        Player player = new Player("alice");
        SetCollectionFoodBC building = new SetCollectionFoodBC(
                10,
                Era.I,
                3,
                6,
                BuildingCardType.SetCollectionFoodBC,
                false
        );

        player.addBuilding(building);

        assertEquals(1, player.getOwnedBuildings().size());
        assertSame(building, player.getOwnedBuildings().get(0));
    }

    //Test that character lists are exposed as unmodifiable views.
    @Test
    void characterLists_shouldBeUnmodifiable() {
        Player player = new Player("alice");
        player.addCharacter(createInventor(11));

        assertThrows(UnsupportedOperationException.class,
                () -> player.getInventors().add(createInventor(12)));
    }

    //Test that owned buildings list is exposed as an unmodifiable view.
    @Test
    void ownedBuildings_shouldBeUnmodifiable() {
        Player player = new Player("alice");
        player.addBuilding(new SetCollectionFoodBC(
                20,
                Era.I,
                3,
                6,
                BuildingCardType.SetCollectionFoodBC,
                false
        ));

        assertThrows(UnsupportedOperationException.class,
                () -> player.getOwnedBuildings().add(new SetCollectionFoodBC(
                        21,
                        Era.I,
                        3,
                        6,
                        BuildingCardType.SetCollectionFoodBC,
                        false
                )));
    }

    //Test that players with the same nickname are equal
    //and have the same hash code.
    @Test
    void equalsAndHashCode_shouldDependOnlyOnNickname() {
        Player first = new Player("alice");
        Player second = new Player("alice");
        Player third = new Player("bob");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        assertNotEquals(first, third);
        assertNotEquals(first, null);
        assertNotEquals(first, "alice");
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
        return new Builder(id, Era.I, CharacterType.BUILDER, 0, 0);
    }

    private Artist createArtist(int id) {
        return new Artist(id, Era.I, CharacterType.ARTIST);
    }

    private Hunter createHunter(int id) {
        return new Hunter(id, Era.I, CharacterType.HUNTER, false);
    }
}