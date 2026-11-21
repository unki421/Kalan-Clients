package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanClickGuiEntries {

    private static final List<KalanClickGuiEntry> ENTRIES =
            new ArrayList<>();

    static {
        register(KalanHudPanel.WATERMARK);
        register(KalanHudPanel.COORDINATES);
        register(KalanHudPanel.KEYSTROKES);
        register(KalanHudPanel.MOUSE);
        register(KalanHudPanel.POTIONS);
        register(KalanHudPanel.ARMOR);
        register(KalanHudPanel.TARGET_HUD);
        register(KalanHudPanel.ARRAY_LIST);
    }

    private KalanClickGuiEntries() {
    }

    private static void register(KalanHudPanel panel) {
        ENTRIES.add(new KalanClickGuiEntry(
                panel.getTitle(),
                panel.getDescription(),
                panel
        ));
    }

    public static List<KalanClickGuiEntry> getAll() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static KalanClickGuiEntry find(String title) {
        if (title == null) {
            return null;
        }

        for (KalanClickGuiEntry entry : ENTRIES) {
            if (entry.getTitle().equalsIgnoreCase(title)) {
                return entry;
            }
        }

        return null;
    }

    public static List<KalanClickGuiEntry> getEnabled() {
        List<KalanClickGuiEntry> result = new ArrayList<>();

        for (KalanClickGuiEntry entry : ENTRIES) {
            if (entry.isEnabled()) {
                result.add(entry);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public static int size() {
        return ENTRIES.size();
    }
}
