package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.cards.Card;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.Visitor;
import org.example.model.interfaces.Visitable;

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

    protected final CharacterType characterType;
    protected boolean newCardInSet = false;


    public Character(int id, Era era, CharacterType characterType) {
        super(id, era);
        this.characterType = characterType;
    }

    public abstract void accept(Visitor visitor);

    @Override
    public boolean isCharacter() { return true; }


    public CharacterType getCharacterType() {
        return characterType;
    }


    public boolean getNewCardInSet() {
        return newCardInSet;
    }


    public void setNewCardInSet(boolean newCardInSet) {
        this.newCardInSet = newCardInSet;
    }

    @Override
    public String toString() {
        return "%s [id: %d]\n".formatted(this.characterType, this.getId());
    }
}
