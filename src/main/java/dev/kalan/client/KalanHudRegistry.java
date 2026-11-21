package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanHudRegistry {

    private static final List<KalanHudElement> ELEMENTS = new ArrayList<>();

    static {
        register(new KalanHudElement(KalanHudPanel.WATERMARK, 6, 6));
        register(new KalanHudElement(KalanHudPanel.INFO, 6, 24));
        register(new KalanHudElement(KalanHudPanel.COORDINATES, 6, 66));
        register(new KalanHudElement(KalanHudPanel.KEYSTROKES, 6, 110));
        register(new KalanHudElement(KalanHudPanel.MOUSE, 6, 178));
        register(new KalanHudElement(KalanHudPanel.POTIONS, 6, 216));
        register(new KalanHudElement(KalanHudPanel.ARMOR, 170, 216));
        register(new KalanHudElement(KalanHudPanel.ARRAY_LIST, 500, 6));
        find(KalanHudPanel.INFO).setVisible(false);
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
