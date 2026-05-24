package org.example.server;

/**
 * Listener notified when a match reaches the game-over phase.
 */
public interface GameOverListener {
    /**
     * Called after the game-over phase has been processed.
     *
     * @param controller the controller that finished the match
     */
    void onGameOver(ServerController controller);
}
