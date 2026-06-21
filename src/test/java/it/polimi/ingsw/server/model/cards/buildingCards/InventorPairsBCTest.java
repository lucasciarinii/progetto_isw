package it.polimi.ingsw.server.model.cards.buildingCards;

import it.polimi.ingsw.server.model.cards.characters.Inventor;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.enums.InventionType;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventorPairsBCTest {


    @Test
    @DisplayName("Zero inventors: food remains 5 after applyEffect")
    void testZeroInventors_foodRemainsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match);

        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("One inventor: food remains 5 after applyEffect")
    void testOneInventor_foodRemainsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        Inventor inventor = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(inventor);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match);

        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("Two equal inventors before card: food remains 5 (ignores pre-existing pairs)")
    void testTwoEqualInventorsPre_foodRemainsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        // PRE: Complete ARROW pair already exists
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow1);
        player.addCharacter(arrow2);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match);

        assertEquals(5, player.getFood()); // Ignores pre-existing pairs
    }

    @Test
    @DisplayName("Zero inventors then one inventor: food remains 5")
    void testZeroToOneInventor_foodRemainsFive() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 0 inventors

        Inventor inventor = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(inventor);
        card.applyEffect(player, match); // Still 1 inventor, no pairs

        assertEquals(5, player.getFood());
    }

    @Test
    @DisplayName("Zero inventors then two equals: +3 food (5→8)")
    void testZeroToTwoEqualInventors_foodToEight() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 0 inventors

        // POST: Complete 1 ARROW pair
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow1);
        player.addCharacter(arrow2);
        card.applyEffect(player, match);

        assertEquals(8, player.getFood()); // 5 + 3 = 1 new pair
    }

    @Test
    @DisplayName("One pre-existing pair then two new pairs: +6 food (5→11)")
    void testOnePrePairTwoNewPairs_foodToEleven() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        // PRE: 1 complete ARROW pair
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow1);
        player.addCharacter(arrow2);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 1 pre-existing pair → 0 new

        // POST: 2 new pairs (BOAT + HOOK)
        Inventor boat1 = new Inventor(12, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor boat2 = new Inventor(71, Era.III, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor hook1 = new Inventor(23, Era.II, CharacterType.INVENTOR, InventionType.HOOK);
        Inventor hook2 = new Inventor(89, Era.III, CharacterType.INVENTOR, InventionType.HOOK);

        player.addCharacter(boat1);
        player.addCharacter(boat2);
        player.addCharacter(hook1);
        player.addCharacter(hook2);
        card.applyEffect(player, match);

        assertEquals(11, player.getFood()); // 5 + 6 = 2 new pairs
    }

    @Test
    @DisplayName("Four new complete pairs: +12 food (5→17)")
    void testFourNewPairs_foodToSeventeen() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        // PRE: 4 single inventors (different types)
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor boat1 = new Inventor(12, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor hook1 = new Inventor(23, Era.II, CharacterType.INVENTOR, InventionType.HOOK);
        Inventor jewels1 = new Inventor(34, Era.I, CharacterType.INVENTOR, InventionType.JEWELS);
        player.addCharacter(arrow1);
        player.addCharacter(boat1);
        player.addCharacter(hook1);
        player.addCharacter(jewels1);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 0 pairs

        // POST: Complete all 4 pairs
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor boat2 = new Inventor(45, Era.II, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor hook2 = new Inventor(89, Era.III, CharacterType.INVENTOR, InventionType.HOOK);
        Inventor jewels2 = new Inventor(67, Era.III, CharacterType.INVENTOR, InventionType.JEWELS);
        player.addCharacter(arrow2);
        player.addCharacter(boat2);
        player.addCharacter(hook2);
        player.addCharacter(jewels2);

        card.applyEffect(player, match);
        assertEquals(17, player.getFood()); // 5 + 12 = 4 new pairs
    }

    @Test
    @DisplayName("One pre-existing pair + one new pair: only +3 food")
    void testOnePrePairOneNew_only3Food() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        // PRE: Complete ARROW pair
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow1);
        player.addCharacter(arrow2);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 1 pre-existing → 0 new

        // POST: 1 new BOAT pair
        Inventor boat1 = new Inventor(12, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor boat2 = new Inventor(45, Era.II, CharacterType.INVENTOR, InventionType.BOAT);
        player.addCharacter(boat1);
        player.addCharacter(boat2);
        card.applyEffect(player, match);

        assertEquals(8, player.getFood()); // 5 + 3 = 1 new pair only
    }

    @Test
    @DisplayName("Three same invention type: only 1 pair (+3 food)")
    void testThreeSameInvention_onePair3Food() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);

        // PRE: 1 ARROW
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow1);
        card.applyEffect(player, match); // 0 pairs

        // POST: +2 ARROW = 3 total (1 pair + 1 leftover)
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor arrow3 = new Inventor(99, Era.II, CharacterType.INVENTOR, InventionType.ARROW);
        player.addCharacter(arrow2);
        player.addCharacter(arrow3);
        card.applyEffect(player, match);

        assertEquals(8, player.getFood()); // 5 + 3 = 1 pair only
    }

    @Test
    @DisplayName("Two complete pairs + one incomplete: +6 food")
    void testTwoPairsPlusIncomplete_6Food() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        Player player = match.getPlayers().get(0);
        player.addFood(-player.getFood() + 5);

        // PRE: 3 single inventors
        Inventor arrow1 = new Inventor(13, Era.I, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor boat1 = new Inventor(12, Era.I, CharacterType.INVENTOR, InventionType.BOAT);
        Inventor hook1 = new Inventor(23, Era.II, CharacterType.INVENTOR, InventionType.HOOK);
        player.addCharacter(arrow1);
        player.addCharacter(boat1);
        player.addCharacter(hook1);

        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);
        player.addBuilding(card);
        card.applyEffect(player, match); // 0 pairs

        // POST: Complete 2 pairs + 1 incomplete
        Inventor arrow2 = new Inventor(72, Era.III, CharacterType.INVENTOR, InventionType.ARROW);
        Inventor boat2 = new Inventor(45, Era.II, CharacterType.INVENTOR, InventionType.BOAT);
        player.addCharacter(arrow2);
        player.addCharacter(boat2);

        card.applyEffect(player, match);
        assertEquals(11, player.getFood()); // 5 + 6 = 2 complete pairs
    }

    @Test
    @DisplayName("Test that inventorPairsBC.toString returns the correct string")
    void testToString() {
        InventorPairsBC card = new InventorPairsBC(8, Era.I, 0, 0, BuildingCardType.CavePaintingEventBoostBC, false);
        assertTrue(card.toString().endsWith("\tEffect: get +3 food each time you get an inventors pair with same invention\n"));
    }

    @Test
    @DisplayName("player null must throw an exception")
    void playerNull() {
        Match match = new Match(List.of(new Player("Alice"), new Player("Bob")));
        InventorPairsBC card = new InventorPairsBC(1, Era.I, 0, 0, BuildingCardType.InventorComboBC, false);

        assertThrows(IllegalArgumentException.class, () -> card.applyEffect(null, match));
    }
}