package dev.kalan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KalanClickGuiEntries {

    private static final List<KalanClickGuiEntry> HUD_ENTRIES = new ArrayList<>();
    private static final List<KalanClickGuiEntry> COMBAT_ENTRIES = new ArrayList<>();
    private static final List<KalanClickGuiEntry> VISUAL_ENTRIES = new ArrayList<>();

    static {
        HUD_ENTRIES.add(new KalanClickGuiEntry(
                "HUD",
                "Общий переключатель HUD; ПКМ открывает настройки компонентов",
                List.of(
                        KalanHudPanel.WATERMARK,
                        KalanHudPanel.COORDINATES,
                        KalanHudPanel.KEYSTROKES,
                        KalanHudPanel.POTIONS,
                        KalanHudPanel.ARMOR,
                        KalanHudPanel.ARRAY_LIST
                )
        ));

        COMBAT_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.CUSTOM_CROSSHAIR));
        COMBAT_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.HIT_PARTICLES));
        COMBAT_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.TARGET_ESP));

        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.CUSTOM_SKY));
        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.CHINA_HAT));
        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.BLOCK_HIGHLIGHT));
        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.PLAYER_TRAILS));
        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.JUMP_CIRCLE));
        VISUAL_ENTRIES.add(new KalanClickGuiEntry(KalanVisualFeature.SNOW));
    }

    private KalanClickGuiEntries() {
    }

    private static void register(List<KalanClickGuiEntry> destination, KalanHudPanel panel) {
        destination.add(new KalanClickGuiEntry(
                panel.getTitle(),
                panel.getDescription(),
                panel
        ));
    }

    public static List<KalanClickGuiEntry> getAll() {
        List<KalanClickGuiEntry> all = new ArrayList<>(HUD_ENTRIES);
        all.addAll(COMBAT_ENTRIES);
        return Collections.unmodifiableList(all);
    }

    public static List<KalanClickGuiEntry> getHudEntries() {
        return Collections.unmodifiableList(HUD_ENTRIES);
    }

    public static List<KalanClickGuiEntry> getCombatEntries() {
        return Collections.unmodifiableList(COMBAT_ENTRIES);
    }

    public static List<KalanClickGuiEntry> getVisualEntries() {
        return Collections.unmodifiableList(VISUAL_ENTRIES);
    }

    public static KalanClickGuiEntry find(String title) {
        if (title == null) {
            return null;
        }

        for (KalanClickGuiEntry entry : getAll()) {
            if (entry.getTitle().equalsIgnoreCase(title)) {
                return entry;
            }
        }

        return null;
    }

    public static List<KalanClickGuiEntry> getEnabled() {
        List<KalanClickGuiEntry> result = new ArrayList<>();

        for (KalanClickGuiEntry entry : getAll()) {
            if (entry.isEnabled()) {
                result.add(entry);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public static int size() {
        return HUD_ENTRIES.size() + COMBAT_ENTRIES.size();
    }
}
