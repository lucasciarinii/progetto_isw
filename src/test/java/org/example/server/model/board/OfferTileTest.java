package org.example.server.model.board;

import org.example.server.model.enums.OfferEffect;
import org.example.server.model.match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileTest {

    private OfferTile offerTile;
    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        // Initialize the variables needed for most of the tests
        offerTile = new OfferTile(OfferEffect.FOOD);
        testPlayer1 = new Player("Alice");
        testPlayer2 = new Player("Bob");
    }

    @Test
    void testConstructor_WithValidOfferEffect() {
        // Verify that the creation is correct and the initial state is right
        OfferTile tile = new OfferTile(OfferEffect.D);

        assertNotNull(tile, "The tile should not be null");
        assertEquals(OfferEffect.D, tile.getOfferEffect(), "The effect should be the one passed to the constructor");
        assertNull(tile.getPlayer(), "Initially there should be no player on the tile");
    }

    @Test
    void testConstructor_WithNullOfferEffect_ThrowsException() {
        // Verify that a NullPointerException is thrown with the correct message
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new OfferTile(null);
        });

        assertEquals("offerEffect cannot be null", exception.getMessage());
    }

    @Test
    void testPlacePlayer_OnEmptyTile() {
        // Verify the normal placement of a player
        offerTile.placePlayer(testPlayer1);

        assertNotNull(offerTile.getPlayer());
        assertEquals(testPlayer1, offerTile.getPlayer(), "The returned player should be testPlayer1");
    }

    @Test
    void testPlacePlayer_OnAlreadyTakenTile_ThrowsException() {
        // Place the first player
        offerTile.placePlayer(testPlayer1);

        // Try to place another one and expect an exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            offerTile.placePlayer(testPlayer2);
        });

        assertEquals("tile already taken", exception.getMessage());
        // Verify that the original player was not overwritten
        assertEquals(testPlayer1, offerTile.getPlayer());
    }

    @Test
    void testRemovePlayer_WhenTileIsTaken() {
        // Setup: place a player
        offerTile.placePlayer(testPlayer1);
        assertNotNull(offerTile.getPlayer());

        // Remove and verify
        offerTile.removePlayer();
        assertNull(offerTile.getPlayer(), "The player should be null after removal");
    }

    @Test
    void testRemovePlayer_WhenTileIsEmpty() {
        // Verify that removing a player from an empty tile does not cause errors
        assertNull(offerTile.getPlayer());

        // It should not throw any exception
        assertDoesNotThrow(() -> offerTile.removePlayer());

        // The state must remain unchanged
        assertNull(offerTile.getPlayer());
    }
}