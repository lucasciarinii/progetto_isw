package org.example.server;

public class ServerLogger {

    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";

    public static void server(String msg) {
        System.out.println(CYAN + "[SERVER] " + RESET + msg);
    }

    public static void lobby(String msg) {
        System.out.println(YELLOW + "[LOBBY]  " + RESET + msg);
    }

    public static void game(String msg) {
        System.out.println(GREEN + "[GAME]   " + RESET + msg);
    }

    public static void error(String msg) {
        System.err.println(RED + "[ERROR]  " + RESET + msg);
    }

    public static void db_error(String msg) {
        System.err.println(RED + "[DB ERROR]  " + RESET + msg);
    }
}