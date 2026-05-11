package org.example.network;

import org.example.server.ServerNotifier;

public interface ServerNetworkAdapter extends ServerNotifier {

    // Server starts on a port
    void start(int port) throws Exception;

    // Server stops
    void stop() throws Exception;
}
