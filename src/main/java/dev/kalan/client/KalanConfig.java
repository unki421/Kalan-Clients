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
    private boolean chinaHatEnabled = false;
    private int chinaHatColor = 0xFFFFAA00;
    private float chinaHatSize = 1.0f;
    private float chinaHatHeight = 0.0f;
    private boolean blockHighlightEnabled = false;
    private int blockHighlightColor = 0x8855AAFF;
    private boolean playerTrailsEnabled = false;
    private int playerTrailsColor = 0xFFB56BFF;
    private boolean jumpCircleEnabled = false;
    private int jumpCircleColor = 0xFFB56BFF;
    private float jumpCircleSize = 1.0f;
    private boolean snowEnabled = false;
    private int snowIntensity = 3;
    private boolean customCrosshairEnabled = false;
    private int customCrosshairColor = 0xFFB56BFF;
    private int customCrosshairSize = 6;
    private int customCrosshairGap = 3;
    private int customCrosshairThickness = 2;
    private boolean hitParticlesEnabled = false;
    private int hitParticlesStyle = 0;
    private int hitParticlesAmount = 8;
    private boolean targetEspEnabled = false;
    private int targetEspStyle = 0;
    private int targetEspColor = 0xFFB56BFF;
    private float targetEspSize = 1.0f;
    private float targetEspSpeed = 1.0f;

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
        chinaHatSize = clamp(chinaHatSize, 0.5f, 2.0f);
        chinaHatHeight = clamp(chinaHatHeight, -0.5f, 0.8f);
        if (chinaHatColor == 0) chinaHatColor = 0xFFFFAA00;
        if (blockHighlightColor == 0) blockHighlightColor = 0x8855AAFF;
        if (playerTrailsColor == 0) playerTrailsColor = 0xFFB56BFF;
        if (jumpCircleColor == 0) jumpCircleColor = 0xFFB56BFF;
        jumpCircleSize = clamp(jumpCircleSize, 0.5f, 2.0f);
        if (snowIntensity <= 0) snowIntensity = 3;
        snowIntensity = Math.max(1, Math.min(5, snowIntensity));
        if (customCrosshairColor == 0) customCrosshairColor = 0xFFB56BFF;
        customCrosshairSize = Math.max(2, Math.min(14, customCrosshairSize));
        customCrosshairGap = Math.max(0, Math.min(12, customCrosshairGap));
        customCrosshairThickness = Math.max(1, Math.min(5, customCrosshairThickness));
        hitParticlesStyle = Math.max(0, Math.min(2, hitParticlesStyle));
        hitParticlesAmount = Math.max(2, Math.min(20, hitParticlesAmount));
        targetEspStyle = Math.max(0, Math.min(2, targetEspStyle));
        if (targetEspColor == 0) targetEspColor = 0xFFB56BFF;
        targetEspSize = clamp(targetEspSize, 0.5f, 2.0f);
        targetEspSpeed = clamp(targetEspSpeed, 0.25f, 2.5f);
        if (customSkyPreset == null || !KalanSkyPreset.isValid(customSkyPreset)) customSkyPreset = KalanSkyPreset.DAY.name();

        if (accentColor == 0) {
            accentColor = KalanTheme.ACCENT_DEFAULT;
        }
    }

    public static String exportProfileSettings() {
        return GSON.toJson(INSTANCE);
    }

    public static boolean importProfileSettings(String json) {
        try {
            KalanConfig loaded = GSON.fromJson(json, KalanConfig.class);
            if (loaded == null) return false;
            loaded.validate();
            INSTANCE = loaded;
            save();
            return true;
        } catch (Exception e) {
            KalanCore.LOGGER.error("Failed to import Kalan profile settings", e);
            return false;
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

    public boolean isChinaHatEnabled() { return chinaHatEnabled; }
    public void setChinaHatEnabled(boolean value) { chinaHatEnabled = value; save(); }
    public int getChinaHatColor() { return chinaHatColor; }
    public void setChinaHatColor(int value) { chinaHatColor = value; save(); }
    public float getChinaHatSize() { return chinaHatSize; }
    public void setChinaHatSize(float value) { chinaHatSize = clamp(value, 0.5f, 2.0f); save(); }
    public float getChinaHatHeight() { return chinaHatHeight; }
    public void setChinaHatHeight(float value) { chinaHatHeight = clamp(value, -0.5f, 0.8f); save(); }

    public boolean isBlockHighlightEnabled() { return blockHighlightEnabled; }
    public void setBlockHighlightEnabled(boolean value) { blockHighlightEnabled = value; save(); }
    public int getBlockHighlightColor() { return blockHighlightColor; }
    public void setBlockHighlightColor(int value) { blockHighlightColor = value; save(); }

    public boolean isPlayerTrailsEnabled() { return playerTrailsEnabled; }
    public void setPlayerTrailsEnabled(boolean value) { playerTrailsEnabled = value; save(); }
    public int getPlayerTrailsColor() { return playerTrailsColor; }
    public void setPlayerTrailsColor(int value) { playerTrailsColor = value; save(); }

    public boolean isJumpCircleEnabled() { return jumpCircleEnabled; }
    public void setJumpCircleEnabled(boolean value) { jumpCircleEnabled = value; save(); }
    public int getJumpCircleColor() { return jumpCircleColor; }
    public void setJumpCircleColor(int value) { jumpCircleColor = value; save(); }
    public float getJumpCircleSize() { return jumpCircleSize; }
    public void setJumpCircleSize(float value) { jumpCircleSize = clamp(value, 0.5f, 2.0f); save(); }

    public boolean isSnowEnabled() { return snowEnabled; }
    public void setSnowEnabled(boolean value) { snowEnabled = value; save(); }
    public int getSnowIntensity() { return snowIntensity; }
    public void setSnowIntensity(int value) { snowIntensity = Math.max(1, Math.min(5, value)); save(); }

    public boolean isCustomCrosshairEnabled() { return customCrosshairEnabled; }
    public void setCustomCrosshairEnabled(boolean value) { customCrosshairEnabled = value; save(); }
    public int getCustomCrosshairColor() { return customCrosshairColor; }
    public void setCustomCrosshairColor(int value) { customCrosshairColor = value; save(); }
    public int getCustomCrosshairSize() { return customCrosshairSize; }
    public void setCustomCrosshairSize(int value) { customCrosshairSize = Math.max(2, Math.min(14, value)); save(); }
    public int getCustomCrosshairGap() { return customCrosshairGap; }
    public void setCustomCrosshairGap(int value) { customCrosshairGap = Math.max(0, Math.min(12, value)); save(); }
    public int getCustomCrosshairThickness() { return customCrosshairThickness; }
    public void setCustomCrosshairThickness(int value) { customCrosshairThickness = Math.max(1, Math.min(5, value)); save(); }

    public boolean isHitParticlesEnabled() { return hitParticlesEnabled; }
    public void setHitParticlesEnabled(boolean value) { hitParticlesEnabled = value; save(); }
    public int getHitParticlesStyle() { return hitParticlesStyle; }
    public void setHitParticlesStyle(int value) { hitParticlesStyle = Math.max(0, Math.min(2, value)); save(); }
    public int getHitParticlesAmount() { return hitParticlesAmount; }
    public void setHitParticlesAmount(int value) { hitParticlesAmount = Math.max(2, Math.min(12, value)); save(); }

    public boolean isTargetEspEnabled() { return targetEspEnabled; }
    public void setTargetEspEnabled(boolean value) { targetEspEnabled = value; save(); }
    public int getTargetEspStyle() { return targetEspStyle; }
    public void setTargetEspStyle(int value) { targetEspStyle = Math.max(0, Math.min(2, value)); save(); }
    public int getTargetEspColor() { return targetEspColor; }
    public void setTargetEspColor(int value) { targetEspColor = value; save(); }
    public float getTargetEspSize() { return targetEspSize; }
    public void setTargetEspSize(float value) { targetEspSize = clamp(value, 0.5f, 2.0f); save(); }
    public float getTargetEspSpeed() { return targetEspSpeed; }
    public void setTargetEspSpeed(float value) { targetEspSpeed = clamp(value, 0.25f, 2.5f); save(); }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
