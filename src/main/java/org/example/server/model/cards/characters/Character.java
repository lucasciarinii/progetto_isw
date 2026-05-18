package org.example.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.server.model.cards.Card;
import org.example.server.model.enums.CharacterType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitable;
import org.example.server.model.interfaces.Visitor;

/**
 * Base class for character cards.
 */
// Indicates to Jackson to use the "class_type" field to decide the subclass
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "CharacterType"
)
// Map values of the "class_type" field to the actual Java classes
@JsonSubTypes({
        @JsonSubTypes.Type(value = Artist.class, name = "Artist"),
        @JsonSubTypes.Type(value = Builder.class, name = "Builder"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "Gatherer"),
        @JsonSubTypes.Type(value = Hunter.class, name = "Hunter"),
        @JsonSubTypes.Type(value = Inventor.class, name = "Inventor"),
        @JsonSubTypes.Type(value = Shaman.class, name = "Shaman"),
})

public abstract class Character extends Card implements Visitable {

    /** Character type associated with the card. */
    protected final CharacterType characterType;
    /** True if this card completed a new set for the owner. */
    protected boolean newCardInSet = false;


    /**
     * Creates a character card.
     *
     * @param id card id
     * @param era card era
     * @param characterType character type
     */
    public Character(int id, Era era, CharacterType characterType) {
        super(id, era);
        this.characterType = characterType;
    }

    /**
     * Accepts a visitor for double dispatch.
     *
     * @param visitor visitor instance
     */
    public abstract void accept(Visitor visitor);

    /**
     * @return true because this is a character card
     */
    @Override
    public boolean isCharacter() { return true; }


    /**
     * @return character type
     */
    public CharacterType getCharacterType() {
        return characterType;
    }


    /**
     * @return true if the card completed a new set
     */
    public boolean getNewCardInSet() {
        return newCardInSet;
    }


    /**
     * Sets whether the card completed a new set.
     *
     * @param newCardInSet true if it completed a new set
     */
    public void setNewCardInSet(boolean newCardInSet) {
        this.newCardInSet = newCardInSet;
    }

    @Override
    public String toString() {
        return "%s [id: %d] {ERA %s}\n".formatted(this.characterType, this.getId(), this.getEra());
    }
}
