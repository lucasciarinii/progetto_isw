package org.example.network;

/**
 * Supported communication protocols for client-server connections.
 */
public enum CommunicationProtocol {
    RMI("RMI"),
    SOCKET("SOCKET");

    private final String protocol;

    CommunicationProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the protocol name as a string.
     *
     * @return the protocol name
     */
    public String getProtocol() {
        return protocol;
    }
}
