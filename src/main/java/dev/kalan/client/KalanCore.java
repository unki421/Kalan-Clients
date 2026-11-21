package dev.kalan.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KalanCore {

    public static final Logger LOGGER =
            LoggerFactory.getLogger("Kalan Client");

    private static boolean initialized;

    private KalanCore() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        LOGGER.info("Starting Kalan Client...");

        KalanConfig.load();
        KalanHudLayoutStore.load();
        KalanHudRenderer.initialize();
        KalanHudDragHandler.initialize();
        KalanClickGuiKeybind.initialize();

        initialized = true;

        LOGGER.info("Kalan Client initialized successfully.");
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
