package dev.kalan.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KalanConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("kalan-client.json");

    private static KalanConfig INSTANCE = new KalanConfig();

    private boolean showFps = true;
    private boolean showCoordinates = true;
    private boolean showKeystrokes = true;
    private boolean showCps = true;

    private int accentColor = KalanTheme.ACCENT_DEFAULT;
    private float guiScale = 1.0f;
    private boolean customSkyEnabled = false;
    private String customSkyPreset = "DAY";

    private KalanConfig() {
    }

    public static KalanConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            KalanConfig loaded = GSON.fromJson(json, KalanConfig.class);

            if (loaded != null) {
                INSTANCE = loaded;
                INSTANCE.validate();
            }
        } catch (Exception e) {
            KalanCore.LOGGER.error("Failed to load Kalan config", e);
        }
    }

    private void validate() {
        guiScale = clamp(guiScale, 0.5f, 2.0f);
        if (customSkyPreset == null || !KalanSkyPreset.isValid(customSkyPreset)) customSkyPreset = KalanSkyPreset.DAY.name();

        if (accentColor == 0) {
            accentColor = KalanTheme.ACCENT_DEFAULT;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            KalanCore.LOGGER.error("Failed to save Kalan config", e);
        }
    }

    public boolean isShowFps() {
        return showFps;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
        save();
    }

    public boolean isShowCoordinates() {
        return showCoordinates;
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
        save();
    }

    public boolean isShowKeystrokes() {
        return showKeystrokes;
    }

    public void setShowKeystrokes(boolean showKeystrokes) {
        this.showKeystrokes = showKeystrokes;
        save();
    }

    public boolean isShowCps() {
        return showCps;
    }

    public void setShowCps(boolean showCps) {
        this.showCps = showCps;
        save();
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        save();
    }

    public float getGuiScale() {
        return guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = clamp(guiScale, 0.5f, 2.0f);
        save();
    }

    public boolean isCustomSkyEnabled() { return customSkyEnabled; }
    public void setCustomSkyEnabled(boolean enabled) { customSkyEnabled = enabled; save(); }
    public String getCustomSkyPreset() { return customSkyPreset; }
    public void setCustomSkyPreset(String preset) { if (preset != null && KalanSkyPreset.isValid(preset)) { customSkyPreset = preset; save(); } }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
