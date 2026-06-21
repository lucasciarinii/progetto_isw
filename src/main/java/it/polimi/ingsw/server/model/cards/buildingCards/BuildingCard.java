package it.polimi.ingsw.server.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.server.model.cards.Card;
import it.polimi.ingsw.server.model.enums.BuildingCardType;
import it.polimi.ingsw.server.model.enums.Era;
import it.polimi.ingsw.server.model.interfaces.Visitable;
import it.polimi.ingsw.server.model.interfaces.Visitor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.Player;

/**
 * Base class for building cards with persistent effects.
 */
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

    /** Food cost to acquire the building. */
    private final int foodCost;
    /** End-game points provided by the building. */
    private final int endPoints;
    /** Building type identifier used in JSON. */
    private final BuildingCardType class_type;
    /** Whether the effect is evaluated at end game. */
    private final boolean isEndGame;

    /**
     * Creates a building card with its properties.
     *
     * @param id card id
     * @param era card era
     * @param foodCost food cost
     * @param endPoints end points
     * @param class_type building type
     * @param isEndGame true if it scores at end game
     */
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

    /**
     * Accepts a visitor for double dispatch.
     *
     * @param visitor visitor instance
     */
    @Override
    public void accept(Visitor visitor)  {
        visitor.visit(this);
    }

    /**
     * @return true because this is a building card
     */
    @Override
    public boolean isBuilding() { return true; }


    /**
     * @return food cost to acquire the building
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * @return end-game points of the building
     */
    public int getEndPoints() {
        return endPoints;
    }

    /**
     * @return true if this building is scored at end game
     */
    public boolean isEndGameBuilding() { return isEndGame; }

    /**
     * @return building type identifier
     */
    public BuildingCardType getClassType() {
        return class_type;
    }

    /**
     * Applies the building effect to the owner in the given match.
     *
     * @param owner building owner
     * @param match current match
     */
    public abstract void applyEffect(Player owner, Match match);

}
