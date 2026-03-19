package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.board.Board;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Context;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class RoundFlowBC extends BuildingCard {
    private final boolean shouldTotem;
    public RoundFlowBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType, @JsonProperty("shouldTotem") boolean st) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
        this.shouldTotem = st;
    }

    public void applyEffect(Player owner, Match match) {
    if (shouldTotem)
    {
        owner.addFood(1);
    }
    else
    {
        //modifica la logica del gioco, da implementare prossimamente
    }
    }
}
