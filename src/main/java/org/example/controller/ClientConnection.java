package org.example.controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final ObjectOutputStream out;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

}
