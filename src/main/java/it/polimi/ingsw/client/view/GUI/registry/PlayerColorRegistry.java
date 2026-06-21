package it.polimi.ingsw.client.view.GUI.registry;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton registry that assigns a JavaFX color to each player.
 */
public class PlayerColorRegistry {

    private static final PlayerColorRegistry INSTANCE = new PlayerColorRegistry();

    // Fixed palette: 5 players max.
    private static final List<Color> PALETTE = List.of(
            Color.web("#e05740"),  // 1 player: Red
            Color.web("#f9c837"),  // 2 player: Yellow
            Color.web("#2698af"),  // 3 player: Green
            Color.web("#0000ff"),  // 4 player: Blue
            Color.web("#7b2d8b")   // 5 player: Purple
    );

    // CSS palette (JavaFX inline).
    private static final List<String> PALETTE_HEX = List.of(
            "#e05740",
            "#f9c837",
            "#2698af",
            "#0000ff",
            "#7b2d8b"
    );

    // nickname -> JavaFX Color
    private final Map<String, Color> colorMap = new HashMap<>();
    // nickname -> hex string for CSS styles
    private final Map<String, String> colorHexMap = new HashMap<>();

    private PlayerColorRegistry() {}

    /**
     * Returns the singleton instance of the registry.
     *
     * @return the shared {@link PlayerColorRegistry} instance
     */
    public static PlayerColorRegistry getInstance() {
        return INSTANCE;
    }


    /**
     * Assigns a color from the built-in palette to each player nickname,
     * in the order the nicknames appear in the list. Any previous assignments
     * are cleared before the new ones are applied.
     * Call this once in GUIHandler.switchToGame() using the player nicknames.
     *
     * @param nicknames the ordered list of player nicknames to assign colors to
     */
    public void init(List<String> nicknames) {
        colorMap.clear();
        colorHexMap.clear();
        for (int i = 0; i < nicknames.size(); i++) {
            String nick = nicknames.get(i);
            colorMap.put(nick, PALETTE.get(i));
            colorHexMap.put(nick, PALETTE_HEX.get(i));
        }
        System.out.println("[PlayerColorRegistry] Colors assigned: " + colorHexMap);
    }

    /**
     * Returns the JavaFX color for the given nickname.
     * If the nickname is not registered, returns gray as a fallback.
     */
    @SuppressWarnings("unused")
    public Color getColor(String nickname) {
        return colorMap.getOrDefault(nickname, Color.GRAY);
    }

    /**
     * Returns the CSS hex color string assigned to the given player,
     * suitable for use in JavaFX inline styles.
     *
     * @param nickname the player's nickname
     * @return the assigned hex color (e.g. {@code "#e05740"}), or {@code "#888888"} if the nickname is not registered
     */
    public String getHex(String nickname) {
        return colorHexMap.getOrDefault(nickname, "#888888");
    }
}