package org.example.network;

/**
 * Client-side adapter that sends player actions to the server.
 */
public interface ClientNetworkAdapter {

    /**
     * Connects a client to the server lobby.
     *
     * @param host       the server host
     * @param port       the server port
     * @throws Exception if the connection fails
     */
    void connect(String host, int port) throws Exception;

    void createLobby(String nickname, int numPlayers) throws Exception;

    void joinLobby(String nickname, String gameID) throws Exception;

    /**
     * Requests to place a totem on a specific offer tile.
     *
     * @param tilePosition the offer tile index
     * @throws Exception if the request fails
     */
    void placeTotemOnOfferTile(int tilePosition) throws Exception;

    /**
     * Sends the chosen cards for the current offer tile action.
     *
     * @param cards the serialized card selection
     * @throws Exception if the request fails
     */
    void offerTileAction(String cards) throws Exception;

    /**
     * Sends the chosen cards for the RoundFlow request.
     *
     * @param cards the serialized card selection
     * @throws Exception if the request fails
     */
    void roundFlowCardRequest(String cards) throws Exception;

    /**
     * Requests to skip the current turn. (used after a client-side check on the number of pickable cards)
     *
     * @throws Exception if the request fails
     */
    void skipTurn() throws Exception;

    /**
     * Disconnects the client from the server.
     *
     * @throws Exception if the disconnection fails
     */
    void disconnect() throws Exception;
}
