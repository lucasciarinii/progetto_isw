package it.polimi.ingsw.client.view.TUI;

/**
 * ANSI color codes used by the TUI.
 */
public class ConsoleColors {
    public static final String RESET = "\u001B[0m";

    // Standard colors
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GREY = "\u001B[90m";
    public static final String PURPLE = "\u001B[35m";

    // Extended (256 colors)
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String MINT = "\u001B[38;5;121m";
    public static final String BROWN = "\u001B[38;5;130m";
}
