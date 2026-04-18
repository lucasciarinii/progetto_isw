package org.example.network.Snapshots;

import java.io.Serial;
import java.io.Serializable;

public class TurnSlotSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int position;
    private final int foodBonus;
    private final int pointsBonus;
    private final String occupantNickname;

    public TurnSlotSnapshot(int position, int foodBonus, int pointsBonus, String occupantNickname) {
        this.position = position;
        this.foodBonus = foodBonus;
        this.pointsBonus = pointsBonus;
        this.occupantNickname = occupantNickname;
    }

    public int getPosition()            { return position; }
    public int getFoodBonus()           { return foodBonus; }
    public int getPointsBonus()         { return pointsBonus; }
    public String getOccupantNickname() { return occupantNickname; }
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
