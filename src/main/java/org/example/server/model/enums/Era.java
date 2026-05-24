package org.example.server.model.enums;

/**
 * Game eras used to stage cards and events.
 */
public enum Era {
    /** Era I. */
    I,
    /** Era II. */
    II,
    /** Era III. */
    III;

    @Override
    public String toString() {
        return switch (this) {
            case I -> "I";
            case II -> "II";
            case III -> "III";
        };
    }
}
