package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.cards.Card;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;
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
    protected boolean newCardInSet;


    public Character(int id, Era era, CharacterType characterType, boolean newCardInSet) {
        super(id, era);
        this.characterType = characterType;
        this.newCardInSet = false;
    }


    public abstract void accept(CardVisitor visitor);


    public CharacterType getCharacterType() {
        return characterType;
    }


    public boolean getNewCardInSet() {
        return newCardInSet;
    }


    public void setNewCardInSet(boolean newCardInSet) {
        this.newCardInSet = newCardInSet;
    }


}
