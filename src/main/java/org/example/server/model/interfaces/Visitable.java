package org.example.server.model.interfaces;

/**
 * Contract for model elements that can accept a {@link Visitor}.
 */
public interface Visitable {
    /**
     * Accepts a visitor to perform double-dispatch operations.
     *
     * @param visitor visitor instance
     */
    void accept(Visitor visitor);
}
