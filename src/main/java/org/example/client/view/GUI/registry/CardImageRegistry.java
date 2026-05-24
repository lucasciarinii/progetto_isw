package org.example.client.view.GUI.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton registry that maps card IDs to JavaFX images.
 *
 * <p>Each card in JSON provides an "image" filename. Call
 * {@code CardImageRegistry.getInstance().init(numPlayers)} when the player
 * count is known to preload the images.</p>
 */
public class CardImageRegistry {

    private static final CardImageRegistry INSTANCE = new CardImageRegistry();

    private static final String[] FIXED_JSON_PATHS = {
            "/json/buildingCards.json",
            "/json/events.json"
    };

    private static final String CHARACTERS_JSON_TEMPLATE = "/json/characters_%dp.json";

    private static final String IMAGE_BASE_PATH = "/images/cards/";

    private final Map<Integer, Image> imageMap = new HashMap<>();
    private Image placeholderImage = null;
    private boolean initialized = false;

    private CardImageRegistry() {}

    public static CardImageRegistry getInstance() {
        return INSTANCE;
    }

    // Load all card images based on the JSON files.
    public void init(int numPlayers) {
        if (initialized) return; // avoid re-initialization

        placeholderImage = loadImage(IMAGE_BASE_PATH + "placeholder.jpg");

        ObjectMapper mapper = new ObjectMapper();
        int loaded = 0, missing = 0;

        // 1. Load the correct characters file based on the number of players
        String charactersPath = String.format(CHARACTERS_JSON_TEMPLATE, numPlayers);
        int[] counts = loadJsonFile(mapper, charactersPath);
        loaded  += counts[0];
        missing += counts[1];

        // 2. Load the fixed files (building and events)
        for (String path : FIXED_JSON_PATHS) {
            counts = loadJsonFile(mapper, path);
            loaded  += counts[0];
            missing += counts[1];
        }

        System.out.printf("[CardImageRegistry] Init with %d players: %d loaded, %d placeholder%n", numPlayers, loaded, missing);
        initialized = true;
    }

    /**
     * Return the Image for the given card ID.
     * If not found, returns the placeholder.
     */
    public Image getImage(int cardId) {
        Image img = imageMap.get(cardId);
        return img != null ? img : placeholderImage;
    }

    public Image getPlaceholder() {
        return placeholderImage;
    }

    /**
     * Read a JSON file and populate the imageMap.
     * @return int array where [0] = loaded, [1] = missing
     */
    // TODO: controllare che ogni configurazione carichi tutte le immagini correttamente
    private int[] loadJsonFile(ObjectMapper mapper, String jsonPath) {
        int loaded = 0, missing = 0;
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            if (is == null) {
                System.err.println("[CardImageRegistry] JSON not found: " + jsonPath);
                return new int[]{0, 0};
            }
            JsonNode root = mapper.readTree(is);
            for (JsonNode node : root) {
                int id = node.get("id").asInt();
                if (node.has("image") && !node.get("image").asText().isBlank()) {
                    String filename = node.get("image").asText();
                    Image img = loadImage(IMAGE_BASE_PATH + filename);
                    imageMap.put(id, img != null ? img : placeholderImage);
                    if (img != null) loaded++; else missing++;
                } else {
                    // If no image is specified, use the placeholder.
                    imageMap.put(id, placeholderImage);
                    missing++;
                }
            }
        } catch (Exception e) {
            System.err.println("[CardImageRegistry] Error on " + jsonPath + ": " + e.getMessage());
        }
        return new int[]{loaded, missing};
    }

    private Image loadImage(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            return new Image(is);
        } catch (Exception e) {
            System.err.println("[CardImageRegistry] Impossible to load: " + resourcePath);
            return null;
        }
    }
}