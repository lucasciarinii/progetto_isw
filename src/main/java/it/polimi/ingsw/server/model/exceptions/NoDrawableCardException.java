package it.polimi.ingsw.server.model.exceptions;

/**
 * Thrown when a draw is requested but no drawable cards are available.
 */
public class NoDrawableCardException extends Exception {
    /**
     * Creates the exception with a descriptive message.
     *
     * @param message error details
     */
    public NoDrawableCardException(String message) {
        super(message);
    }
}
