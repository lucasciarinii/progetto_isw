package org.example.server.model.cards.eventCards;

import org.example.client.view.TUI.ConsoleColors;
import org.example.server.model.enums.Era;
import org.example.server.model.enums.EventEffect;
import org.example.server.model.match.Match;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    private static class DummyEventCard extends EventCard {

        DummyEventCard(int id, Era era, boolean isEraFinal, EventEffect effect) {
            super(id, era, isEraFinal, effect);
        }

        @Override
        public void applyEvent(Match match) {
        }
    }

    //Test that the constructor initializes inherited and own fields correctly.
    @Test
    void constructor_shouldInitializeFieldsCorrectly() {
        DummyEventCard card = new DummyEventCard(10, Era.II, true, EventEffect.SHAMANIC_RITUAL);

        assertEquals(10, card.getId());
        assertEquals(Era.II, card.getEra());
        assertEquals(EventEffect.SHAMANIC_RITUAL, card.getEventEffect());
        assertTrue(card.isEraFinal());
    }

    //Test that EventCard is recognized as an event card.
    @Test
    void isEventCard_shouldReturnTrue() {
        DummyEventCard card = new DummyEventCard(11, Era.I, false, EventEffect.SUSTENANCE);

        assertTrue(card.isEventCard());
    }

    //Test that toString returns the expected formatted string.
    @Test
    void toString_shouldReturnExpectedFormat() {
        DummyEventCard card = new DummyEventCard(12, Era.III, false, EventEffect.HUNT_EVENT);

        String result = card.toString();

        String expected = "%s%s [id: %d] {ERA %s}\n"
                .formatted(ConsoleColors.BROWN, EventEffect.HUNT_EVENT, 12, Era.III);

        assertEquals(expected, result);
    }

    //Test that isEraFinal returns false when configured as false.
    @Test
    void isEraFinal_shouldReturnFalseWhenConfiguredFalse() {
        DummyEventCard card = new DummyEventCard(13, Era.I, false, EventEffect.CAVE_PAINTINGS);

        assertFalse(card.isEraFinal());
    }

    //Test that getEventEffect returns the configured effect for another enum value.
    @Test
    void getEventEffect_shouldReturnConfiguredValue() {
        DummyEventCard card = new DummyEventCard(14, Era.I, false, EventEffect.SUSTENANCE);

        assertEquals(EventEffect.SUSTENANCE, card.getEventEffect());
    }
}