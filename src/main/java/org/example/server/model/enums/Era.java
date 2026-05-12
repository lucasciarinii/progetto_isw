package org.example.server.model.enums;

public enum Era {
    I,
    II,
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
