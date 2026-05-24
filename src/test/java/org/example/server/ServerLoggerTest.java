package org.example.server;

import static org.junit.jupiter.api.Assertions.*;

class ServerLoggerTest{

     @org.junit.jupiter.api.Test
     void server() {
          ServerLogger.server("This is a server log message.");
     }

     @org.junit.jupiter.api.Test
     void lobby() {
          ServerLogger.lobby("This is a lobby log message.");
     }

     @org.junit.jupiter.api.Test
     void game() {
          ServerLogger.game("This is a game log message.");
     }

     @org.junit.jupiter.api.Test
     void error() {
          ServerLogger.error("This is an error log message.");
     }

     @org.junit.jupiter.api.Test
     void db_error() {
          ServerLogger.db_error("This is a database error log message.");
     }{

}}