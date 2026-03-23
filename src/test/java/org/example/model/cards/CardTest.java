package org.example.model.cards;

import junit.framework.TestCase;
import org.example.model.enums.Era;

public class CardTest extends TestCase {

    // Sottoclasse concreta fittizia, solo per i test
    private static class ConcreteCard extends Card {
        public ConcreteCard(int id, Era era) {
            super(id, era);
        }
    }

    public void testCardInitialization() {
        Card card = new ConcreteCard(42, Era.I);
        assertEquals(42, card.getId());
        assertEquals(Era.I, card.getEra());
    }

    public void testDifferentIds() {
        Card card1 = new ConcreteCard(1, Era.I);
        Card card2 = new ConcreteCard(2, Era.I);
        assertFalse(card1.getId() == card2.getId());
    }

    public void testZeroIdIsPreserved() {
        Card card = new ConcreteCard(0, Era.II);
        assertEquals(0, card.getId());
        assertEquals(Era.II, card.getEra());
    }

    public void testNegativeIdIsPreserved() {
        Card card = new ConcreteCard(-7, Era.III);
        assertEquals(-7, card.getId());
        assertEquals(Era.III, card.getEra());
    }

    public void testMinIntegerIdIsPreserved() {
        Card card = new ConcreteCard(Integer.MIN_VALUE, Era.I);
        assertEquals(Integer.MIN_VALUE, card.getId());
    }

    public void testMaxIntegerIdIsPreserved() {
        Card card = new ConcreteCard(Integer.MAX_VALUE, Era.I);
        assertEquals(Integer.MAX_VALUE, card.getId());
    }

    public void testEraIIsPreserved() {
        Card card = new ConcreteCard(10, Era.I);
        assertEquals(Era.I, card.getEra());
    }

    public void testEraIIIsPreserved() {
        Card card = new ConcreteCard(10, Era.II);
        assertEquals(Era.II, card.getEra());
    }

    public void testEraIIIIsPreserved() {
        Card card = new ConcreteCard(10, Era.III);
        assertEquals(Era.III, card.getEra());
    }

    public void testGetterValuesAreStableAcrossMultipleCalls() {
        Card card = new ConcreteCard(99, Era.II);
        assertEquals(99, card.getId());
        assertEquals(99, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(Era.II, card.getEra());
    }

    public void testNullEraIsCurrentlyStoredAsNull() {
        Card card = new ConcreteCard(5, null);
        assertNull(card.getEra());
        assertEquals(5, card.getId());
    }

    public void testAllEraValuesArePreserved() {
        for (Era era : Era.values()) {
            Card card = new ConcreteCard(77, era);
            assertEquals(era, card.getEra());
            assertEquals(77, card.getId());
        }
    }

    public void testSameInputProducesIndependentInstances() {
        Card first = new ConcreteCard(11, Era.II);
        Card second = new ConcreteCard(11, Era.II);

        assertNotSame(first, second);
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getEra(), second.getEra());
    }

    public void testSameIdWithDifferentEraIsPreserved() {
        Card first = new ConcreteCard(31, Era.I);
        Card second = new ConcreteCard(31, Era.III);

        assertEquals(31, first.getId());
        assertEquals(31, second.getId());
        assertNotSame(first.getEra(), second.getEra());
    }

    public void testDifferentIdWithSameEraIsPreserved() {
        Card first = new ConcreteCard(-1, Era.III);
        Card second = new ConcreteCard(1000, Era.III);

        assertFalse(first.getId() == second.getId());
        assertEquals(Era.III, first.getEra());
        assertEquals(Era.III, second.getEra());
    }
}