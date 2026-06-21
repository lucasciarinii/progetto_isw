package it.polimi.ingsw.server.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.enums.CharacterType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.interfaces.Visitable;
import it.polimi.ingsw.server.model.interfaces.Visitor;

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



    @Override
    public String toString() {
        return "%s [id: %d] {ERA %s}\n".formatted(this.characterType, this.getId(), this.getEra());
    }
}
