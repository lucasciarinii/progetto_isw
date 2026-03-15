package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.cards.Card;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

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

public abstract class BuildingCard extends Card {

    private final int foodCost;
    private final int endPoints;
    private final boolean isEndGame;

    public BuildingCard(int id, Era era, int foodCost, int endPoints, boolean isEndGame) {
        super(id, era);
        this.foodCost = foodCost;
        this.endPoints = endPoints;
        this.isEndGame = isEndGame;
    }

    public int getFoodCost() {
        return foodCost;
    }

    public int getEndPoints() {
        return endPoints;
    }

    public boolean isEndGame() {
        return isEndGame;
    }

    public abstract void applyEffect(Player owner, Context context);

}
