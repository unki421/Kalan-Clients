
package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanClickGuiEntries {

    private static final List<KalanClickGuiEntry> ENTRIES =
            new ArrayList<>();

    static {
        register(new KalanClickGuiEntry(
                "Watermark",
                "Название клиента на экране",
                KalanHudPanel.WATERMARK
        ));

        register(new KalanClickGuiEntry(
                "Info",
                "FPS и другая информация",
                KalanHudPanel.INFO
        ));

        register(new KalanClickGuiEntry(
                "Coordinates",
                "Координаты игрока",
                KalanHudPanel.COORDINATES
        ));

        register(new KalanClickGuiEntry(
                "Keystrokes",
                "Отображение клавиш WASD",
                KalanHudPanel.KEYSTROKES
        ));

        register(new KalanClickGuiEntry(
                "Mouse",
                "Отображение нажатий мыши",
                KalanHudPanel.MOUSE
        ));

        register(new KalanClickGuiEntry(
                "Potions",
                "Активные эффекты",
                KalanHudPanel.POTIONS
        ));
    }

    private KalanClickGuiEntries() {
    }

    private static void register(KalanClickGuiEntry entry) {
        ENTRIES.add(entry);
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
