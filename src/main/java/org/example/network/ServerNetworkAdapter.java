package org.example.network;

public interface ServerNetworkAdapter extends ServerNotifier {

    // Server starts
    void start() throws Exception;

    // Server stops
    void stop() throws Exception;
}
