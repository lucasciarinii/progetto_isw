package org.example.client.view.GUI.registry;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton registry: assign a JavaFX color to each player
 */
public class PlayerColorRegistry {

    private static final PlayerColorRegistry INSTANCE = new PlayerColorRegistry();

    // Fixed palette: 5 players max
    private static final List<Color> PALETTE = List.of(
            Color.web("#e05740"),  // 1 player: Red
            Color.web("#f9c837"),  // 2 player: Yellow
            Color.web("#2698af"),  // 3 player: Green
            Color.web("#0000ff"),  // 4 player: Blue
            Color.web("#7b2d8b")   // 5 player: Purple
    );

    // CSS Version (JavaFX inline)
    private static final List<String> PALETTE_HEX = List.of(
            "#e05740",
            "#f9c837",
            "#2698af",
            "#0000ff",
            "#7b2d8b"
    );

    // nickname → Color JavaFX
    private final Map<String, Color>  colorMap    = new HashMap<>();
    // nickname → hex string CSS (per -fx-background-color ecc.)
    private final Map<String, String> colorHexMap = new HashMap<>();

    private PlayerColorRegistry() {}

    public static PlayerColorRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Assign colors to players in the order of the list.
     * Must be called once in GUIHandler.switchToGame() passing update.getPlayers() mapped on nicknames.
     */
    public void init(List<String> nicknames) {
        colorMap.clear();
        colorHexMap.clear();
        for (int i = 0; i < nicknames.size(); i++) {
            String nick = nicknames.get(i);
            colorMap.put(nick,    PALETTE.get(i));
            colorHexMap.put(nick, PALETTE_HEX.get(i));
        }
        System.out.println("[PlayerColorRegistry] Colors assigned: " + colorHexMap);
    }

    /**
     * Return the JavaFX Color of the player with the given nickname.
     * If the nickname is not registered (should not happen), it returns gray as a fallback.
     */
    public Color getColor(String nickname) {
        return colorMap.getOrDefault(nickname, Color.GRAY);
    }

    public String getHex(String nickname) {
        return colorHexMap.getOrDefault(nickname, "#888888");
    }
}