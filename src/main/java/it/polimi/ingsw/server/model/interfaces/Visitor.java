package it.polimi.ingsw.server.model.interfaces;

import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.cards.characters.*;
import it.polimi.ingsw.server.model.cards.characters.*;

/**
 * Visitor for model elements that support double dispatch.
 */
public interface Visitor {
    /**
     * Handles a visit to an inventor character.
     *
     * @param inventor inventor instance
     */
    void visit(Inventor inventor);

    /**
     * Handles a visit to a gatherer character.
     *
     * @param gatherer gatherer instance
     */
    void visit(Gatherer gatherer);

    /**
     * Handles a visit to a shaman character.
     *
     * @param shaman shaman instance
     */
    void visit(Shaman shaman);

    /**
     * Handles a visit to a builder character.
     *
     * @param builder builder instance
     */
    void visit(Builder builder);

    /**
     * Handles a visit to an artist character.
     *
     * @param artist artist instance
     */
    void visit(Artist artist);

    /**
     * Handles a visit to a hunter character.
     *
     * @param hunter hunter instance
     */
    void visit(Hunter hunter);

    /**
     * Handles a visit to a building card.
     *
     * @param building building card instance
     */
    void visit(BuildingCard building);
}
