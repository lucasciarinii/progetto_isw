package it.polimi.ingsw.server;

/**
 * Utility logger for server-side components with colored prefixes.
 */
public class ServerLogger {

    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";

    /**
     * Logs a generic server message.
     *
     * @param msg the message to print
     */
    public static void server(String msg) {
        System.out.println(CYAN + "[SERVER] " + RESET + msg);
    }

    /**
     * Logs a lobby-related message.
     *
     * @param msg the message to print
     */
    public static void lobby(String msg) {
        System.out.println(YELLOW + "[LOBBY]  " + RESET + msg);
    }

    /**
     * Logs a game-related message.
     *
     * @param msg the message to print
     */
    public static void game(String msg) {
        System.out.println(GREEN + "[GAME]   " + RESET + msg);
    }

    /**
     * Logs a generic error message.
     *
     * @param msg the message to print
     */
    public static void error(String msg) {
        System.err.println(RED + "[ERROR]  " + RESET + msg);
    }

    /**
     * Logs a database-related error message.
     *
     * @param msg the message to print
     */
    public static void db_error(String msg) {
        System.err.println(RED + "[DB ERROR]  " + RESET + msg);
    }
}