package org.example.model.cards.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.CharacterType;
import org.example.model.enums.Era;
import org.example.model.interfaces.CardVisitor;

public class Artist extends Character{


    public Artist(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("CharacterType") CharacterType characterType, @JsonProperty("newCardInSet") boolean newCardInSet) {
        super(id, era, characterType, newCardInSet);
    }

    @Override
    public void accept(CardVisitor visitor) {
        visitor.visit(this);
    }


}
