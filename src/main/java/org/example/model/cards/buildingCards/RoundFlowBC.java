package org.example.model.cards.buildingCards;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.enums.BuildingCardType;
import org.example.model.enums.Era;
import org.example.model.match.Match;
import org.example.model.match.Player;

public class RoundFlowBC extends BuildingCard {
    public RoundFlowBC(@JsonProperty("id") int id, @JsonProperty("era") Era era, @JsonProperty("foodCost") int foodCost, @JsonProperty("endPoints") int endPoints, @JsonProperty("isEndGame") boolean isEndGame, @JsonProperty("class_type") BuildingCardType buildingCardType) {
        super(id, era, foodCost, endPoints, isEndGame, buildingCardType);
    }

    public void applyEffect(Player owner, Match match) {
        //modifica la logica del gioco, da implementare prossimamente
    }
}
