package org.example.network;

public enum CommunicationProtocol {
    RMI("RMI"),
    SOCKET("SOCKET");

    private final String protocol;

    CommunicationProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
