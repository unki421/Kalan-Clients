package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanHudRegistry {

    private static final List<KalanHudElement> ELEMENTS = new ArrayList<>();

    static {
        register(new KalanHudElement("fps", 6, 6));
        register(new KalanHudElement("coordinates", 6, 22));
        register(new KalanHudElement("keystrokes", 6, 40));
        register(new KalanHudElement("cps", 6, 82));
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

    public static List<KalanHudElement> getAll() {
        return Collections.unmodifiableList(ELEMENTS);
    }

    public static int size() {
        return ELEMENTS.size();
    }
}
