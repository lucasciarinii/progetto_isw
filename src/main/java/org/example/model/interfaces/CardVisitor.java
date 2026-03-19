package org.example.model.interfaces;

import org.example.model.cards.characters.*;

public interface CardVisitor {
    void visit(Inventor inventor);
    void visit(Gatherer gatherer);
    void visit(Shaman shaman);
    void visit(Builder builder);
    void visit(Artist artist);
    void visit(Hunter hunter);
}
