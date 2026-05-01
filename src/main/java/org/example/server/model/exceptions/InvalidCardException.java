package org.example.server.model.exceptions;

public class InvalidCardException extends Exception {
    public InvalidCardException(String message) {
        super(message);
    }
}