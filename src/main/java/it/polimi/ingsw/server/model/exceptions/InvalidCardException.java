package it.polimi.ingsw.server.model.exceptions;

/**
 * Thrown when a card does not satisfy the required constraints or type.
 */
public class InvalidCardException extends Exception {
    /**
     * Creates the exception with a descriptive message.
     *
     * @param message error details
     */
    public InvalidCardException(String message) {
        super(message);
    }
}