package org.example.network;

/**
 * Abstraction for server-side network adapters (RMI, Socket, or hybrid).
 */
public interface ServerNetworkAdapter extends ServerNotifier {

    /**
     * Starts the server adapter and begins accepting connections.
     *
     * @throws Exception if the adapter fails to start
     */
    void start() throws Exception;

    /**
     * Stops the server adapter and releases its resources.
     *
     * @throws Exception if the adapter fails to stop cleanly
     */
    void stop() throws Exception;
}
