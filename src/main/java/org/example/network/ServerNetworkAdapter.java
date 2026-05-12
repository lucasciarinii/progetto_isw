package org.example.network;

import org.example.server.ServerNotifier;

public interface ServerNetworkAdapter extends ServerNotifier {

    // Server starts
    void start() throws Exception;

    // Server stops
    void stop() throws Exception;
}
