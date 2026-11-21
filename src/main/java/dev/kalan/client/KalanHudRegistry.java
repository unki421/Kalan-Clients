package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanHudRegistry {

    private static final List<KalanHudElement> ELEMENTS = new ArrayList<>();

    static {
        // Default layout follows the supplied Figma reference.
        // Positions are clamped to the active scaled resolution during rendering.
        register(new KalanHudElement(KalanHudPanel.WATERMARK, 10, 10));
        register(new KalanHudElement(KalanHudPanel.INFO, 10, 75));
        register(new KalanHudElement(KalanHudPanel.COORDINATES, 10, 70));
        register(new KalanHudElement(KalanHudPanel.POTIONS, 10, 108));
        register(new KalanHudElement(KalanHudPanel.KEYSTROKES, 715, 8));
        register(new KalanHudElement(KalanHudPanel.MOUSE, 715, 110));
        register(new KalanHudElement(KalanHudPanel.ARRAY_LIST, 688, 118));
        register(new KalanHudElement(KalanHudPanel.TARGET_HUD, 695, 307));
        register(new KalanHudElement(KalanHudPanel.ARMOR, 705, 380));
        find(KalanHudPanel.INFO).setVisible(false);
        // Mouse buttons are already integrated into the Keystrokes panel in the reference.
        find(KalanHudPanel.MOUSE).setVisible(false);
    }
    private KalanHudRegistry() {
    }

    public static void register(KalanHudElement element) {
        if (element == null) {
            throw new IllegalArgumentException("HUD element cannot be null");
        }

        if (find(element.getId()) != null) {
            throw new IllegalArgumentException(
                    "HUD element already registered: " + element.getId()
            );
        }

        ELEMENTS.add(element);
    }

    public static KalanHudElement find(String id) {
        if (id == null) {
            return null;
        }

        for (KalanHudElement element : ELEMENTS) {
            if (element.getId().equalsIgnoreCase(id)) {
                return element;
            }
        }

        return null;
    }

    public static KalanHudElement find(KalanHudPanel panel) {
        return panel == null ? null : find(panel.getId());
    }

    public static boolean setVisible(String id, boolean visible) {
        KalanHudElement element = find(id);

        if (element == null) {
            return false;
        }

        element.setVisible(visible);
        return true;
    }

    public static void setAllVisible(boolean visible) {
        for (KalanHudElement element : ELEMENTS) {
            element.setVisible(visible);
        }
    }

    public static List<KalanHudElement> getAll() {
        return Collections.unmodifiableList(ELEMENTS);
    }

    public static int size() {
        return ELEMENTS.size();
    }
}
