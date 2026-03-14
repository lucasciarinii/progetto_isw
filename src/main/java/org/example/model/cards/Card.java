package org.example.model.cards;

import org.example.model.cards.buildingCards.EndGameBonusBC;
import org.example.model.cards.buildingCards.EventBoostBC;
import org.example.model.cards.buildingCards.RoundFlowBC;
import org.example.model.enums.Era;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Indicates to Jackson to use the "class_type" field to decide the subclass
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class_type"
)
// Map values of the "class_type" field to the actual Java classes
@JsonSubTypes({
        @JsonSubTypes.Type(value = EventBoostBC.class, name = "EventBoostBC"),
        @JsonSubTypes.Type(value = RoundFlowBC.class, name = "RoundFlowBC"),
        @JsonSubTypes.Type(value = EndGameBonusBC.class, name = "EndGameBonusBC"),

        // TODO: ADD REMAINING CARD TYPES
})

public abstract class Card {

    private final int id;
    private final Era era;

    public Card(int id, Era era) {
        this.era = era;
        this.id = id;
    }

    public Era getEra() {
        return era;
    }
    public int getId() { return id; }

}