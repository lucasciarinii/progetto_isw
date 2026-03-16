package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.board.Board;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Player;

public class EndGameBonusBC extends BuildingCard {
    private final boolean shouldDoubleOnBuilders;
    public EndGameBonusBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("shouldDoubleOnBuilders") boolean s) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.shouldDoubleOnBuilders = s;
    }

    public void applyEffect(Player owner, Context context) {

    }
}
