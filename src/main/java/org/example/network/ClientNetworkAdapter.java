package org.example.network;

public interface ClientNetworkAdapter {

    // Client connects to the server
    void connect(String host, int port) throws Exception;

    void createLobby(String nickname, int numPlayers) throws Exception;

    void joinLobby(String nickname, String gameID) throws Exception;

    // Client place a totem on a tile
    void placeTotemOnOfferTile(int tilePosition) throws Exception;

    // Client action
    void offerTileAction(String cards) throws Exception;

    void roundFlowCardRequest(String cards) throws Exception;

    // Client executes an action on a tile
    void skipTurn() throws Exception;

    // Client disconnects
    void disconnect() throws Exception;
}
