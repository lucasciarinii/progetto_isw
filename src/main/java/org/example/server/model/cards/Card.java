package org.example.server.model.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for all game cards.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class Card implements Serializable {

    /** Unique card identifier. */
    private final int id;
    /** Era this card belongs to. */
    private final Era era;
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a card with the given id and era.
     *
     * @param id unique card id
     * @param era card era
     */
    public Card(int id, Era era) {

        this.era = Objects.requireNonNull(era, "Era cannot be null");
        this.id = id;
    }

    /**
     * Returns the card era.
     *
     * @return card era
     */
    public Era getEra() {
        return era;
    }

    /**
     * Returns the card id.
     *
     * @return card id
     */
    public int getId() { return id; }

    /**
     * Accepts a visitor. Default implementation does nothing.
     *
     * @param visitor visitor instance
     */
    public void accept(Visitor visitor) {
        // Default implementation does nothing. Subclasses can override this method to accept visitors.
    }

    /**
     * @return true if this card is a building card
     */
    public boolean isBuilding() { return false; }

    /**
     * @return true if this card is a character card
     */
    public boolean isCharacter() { return false; }

    /**
     * @return true if this card is an event card
     */
    public boolean isEventCard() { return false; }

    /**
     * @return true if this card represents a sustenance event
     */
    public boolean isSustenance() { return false; }
}