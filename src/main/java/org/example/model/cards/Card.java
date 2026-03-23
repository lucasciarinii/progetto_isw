package org.example.model.cards;

import org.example.model.enums.Era;
import java.util.Objects;

public abstract class Card {

    private final int id;
    private final Era era;

    public Card(int id, Era era) {

        this.era = Objects.requireNonNull(era, "Era cannot be null");
        this.id = id;
    }



    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

}