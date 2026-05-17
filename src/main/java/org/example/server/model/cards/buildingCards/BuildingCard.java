package org.example.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.server.model.cards.Card;
import org.example.server.model.enums.BuildingCardType;
import org.example.server.model.enums.Era;
import org.example.server.model.interfaces.Visitable;
import org.example.server.model.interfaces.Visitor;
import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

// Indicates to Jackson to use the "class_type" field to decide the subclass
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class_type"
)
// Map values of the "class_type" field to the actual Java classes
@JsonSubTypes({
        @JsonSubTypes.Type(value = SetCollectionFoodBC.class, name = "SetCollectionFoodBC"),
        @JsonSubTypes.Type(value = SetCollectionEndPointsBC.class, name = "SetCollectionEndPointsBC"),
        @JsonSubTypes.Type(value = SustenanceDiscountBC.class, name = "SustenanceDiscountBC"),
        @JsonSubTypes.Type(value = CharacterEndPointsBC.class, name = "CharacterEndPointsBC"),
        @JsonSubTypes.Type(value = InventorPairsBC.class, name = "InventorPairsBC"),
        @JsonSubTypes.Type(value = ShamanicDoublePointsBC.class, name = "ShamanicDoublePointsBC"),
        @JsonSubTypes.Type(value = ShamanicStarsBC.class, name = "ShamanicStarsBC"),
        @JsonSubTypes.Type(value = ShamanicNoMalusBC.class, name = "ShamanicNoMalusBC"),
        @JsonSubTypes.Type(value = HuntEventBoostBC.class, name = "HuntEventBoostBC"),
        @JsonSubTypes.Type(value = CavePaintingEventBoostBC.class, name = "CavePaintingEventBoostBC"),
        @JsonSubTypes.Type(value = RoundFlowBC.class, name = "RoundFlowBC"),
        @JsonSubTypes.Type(value = RoundFlowTotemBC.class, name = "RoundFlowTotemBC"),
        @JsonSubTypes.Type(value = EndGameBonusBC.class, name = "EndGameBonusBC"),
        @JsonSubTypes.Type(value = EndGameBonus25BC.class, name = "EndGameBonus25BC"),
})

public abstract class BuildingCard extends Card implements Visitable {

    private final int foodCost;
    private final int endPoints;
    private final BuildingCardType class_type;
    private final boolean isEndGame;

    public BuildingCard(int id, Era era, int foodCost, int endPoints, BuildingCardType class_type, boolean isEndGame) {
        super(id, era);
        this.foodCost = foodCost;
        this.endPoints = endPoints;
        this.class_type = class_type;
        this.isEndGame = isEndGame;
    }

    @Override
    public String toString() {
        return "%s [id: %d] {ERA %s}\n\tfood cost: %d, end points: %d\n".formatted(class_type, getId(), getEra(), foodCost, endPoints);
    }

    @Override
    public void accept(Visitor visitor)  {
        visitor.visit(this);
    }

    @Override
    public boolean isBuilding() { return true; }


    public int getFoodCost() {
        return foodCost;
    }

    public int getEndPoints() {
        return endPoints;
    }

    public boolean isEndGameBuilding() { return isEndGame; }

    public BuildingCardType getClassType() {
        return class_type;
    }

    public abstract void applyEffect(Player owner, Match match);

}
