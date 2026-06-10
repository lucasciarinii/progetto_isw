package org.example.network.snapshots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable snapshot of a turn order slot.
 */
@SuppressWarnings("ClassCanBeRecord")
public class TurnSlotSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int position;
    private final int foodBonus;
    private final int pointsBonus;
    private final String occupantNickname;

    @JsonCreator
    public TurnSlotSnapshot(
            @JsonProperty("position") int position,
            @JsonProperty("foodBonus") int foodBonus,
            @JsonProperty("pointsBonus") int pointsBonus,
            @JsonProperty("occupantNickname") String occupantNickname) {
        this.position = position;
        this.foodBonus = foodBonus;
        this.pointsBonus = pointsBonus;
        this.occupantNickname = occupantNickname;
    }

    /** @return the slot position index */
    public int getPosition()            { return position; }
    /** @return the food bonus for the slot */
    public int getFoodBonus()           { return foodBonus; }
    /** @return the points bonus for the slot */
    public int getPointsBonus()         { return pointsBonus; }
    /** @return the nickname of the occupying player, or null */
    public String getOccupantNickname() { return occupantNickname; }
    @JsonIgnore
    public boolean isFree()             { return occupantNickname == null; }

    @Override
    public String toString() {
        if(foodBonus > 0)
            return "[%d] %s\n\tFood bonus: %d".formatted(position + 1, ((occupantNickname != null && !occupantNickname.isEmpty()) ? occupantNickname.toUpperCase() : "empty"), foodBonus);
        else if (foodBonus == 0)
            return "[%d] %s\n\tno bonus".formatted(position + 1, ((occupantNickname != null && !occupantNickname.isEmpty()) ? occupantNickname.toUpperCase() : "empty"));
        else
            return "[%d] %s\n\tMALUS (-1 food / -2 points)".formatted(position + 1, ((occupantNickname != null && !occupantNickname.isEmpty()) ? occupantNickname.toUpperCase() : "empty"));
    }
}
