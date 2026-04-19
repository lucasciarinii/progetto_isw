package org.example.server.model.cards;

import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class Card implements Serializable {

    private final int id;
    private final Era era;
    @Serial
    private static final long serialVersionUID = 1L;

    public Card(int id, Era era) {

        this.era = Objects.requireNonNull(era, "Era cannot be null");
        this.id = id;
    }

    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

    public void accept(Visitor visitor) {
        // Default implementation does nothing. Subclasses can override this method to accept visitors.
    }

    public boolean isBuilding() { return false; }
    public boolean isCharacter() { return false; }
    public boolean isEventCard() { return false; }
    public boolean isSustenance() { return false; }
}