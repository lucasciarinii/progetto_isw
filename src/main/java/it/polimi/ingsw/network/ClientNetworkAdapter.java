package it.polimi.ingsw.network;

/**
 * Client-side adapter that sends player actions to the server.
 */
public interface ClientNetworkAdapter {

    int RMI_PORT = 1099;
    int SOCKET_PORT = 9999;

    /**
     * Connects a client to the server lobby.
     *
     * @param host       the server host
     * @throws Exception if the connection fails
     */
    void connect(String host) throws Exception;

    /**
     * Create a new game lobby.
     * @param nickname the player nickname creating the lobby
     * @param numPlayers the number of players for the new game
     * @throws Exception if the lobby creation fails
     */
    void createLobby(String nickname, int numPlayers) throws Exception;

    /**
     * Joins an existing game lobby.
     *
     * @param nickname the player nickname joining the lobby
     * @param gameID the ID of the lobby to join
     * @throws Exception if the join operation fails
     */
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
