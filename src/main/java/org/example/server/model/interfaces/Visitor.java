package org.example.server.model.interfaces;

import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.cards.characters.*;

public interface Visitor {
    void visit(Inventor inventor);
    void visit(Gatherer gatherer);
    void visit(Shaman shaman);
    void visit(Builder builder);
    void visit(Artist artist);
    void visit(Hunter hunter);

    void visit(BuildingCard building);
}
