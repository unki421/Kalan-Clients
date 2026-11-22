package dev.kalan.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class KalanCustomSky {
    private static boolean initialized;
    private static boolean enabled;
    private static KalanSkyPreset preset = KalanSkyPreset.DAY;
    private static ClientWorld trackedWorld;
    private static long originalTimeOfDay;

    private KalanCustomSky() {
    }

    public static void initialize() {
        if (initialized) return;
        KalanConfig config = KalanConfig.get();
        enabled = config.isCustomSkyEnabled();
        preset = KalanSkyPreset.valueOf(config.getCustomSkyPreset());
        ClientTickEvents.END_CLIENT_TICK.register(KalanCustomSky::tick);
        initialized = true;
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null) {
            trackedWorld = null;
            return;
        }
        if (trackedWorld != client.world) {
            trackedWorld = client.world;
            originalTimeOfDay = trackedWorld.getTimeOfDay();
        }
        if (enabled) {
            trackedWorld.setTime(trackedWorld.getTime(), preset.getTime(), false);
        }
    }

    public static void reloadFromConfig() {
        KalanConfig config = KalanConfig.get();
        enabled = config.isCustomSkyEnabled();
        try {
            preset = KalanSkyPreset.valueOf(config.getCustomSkyPreset());
        } catch (IllegalArgumentException ignored) {
            preset = KalanSkyPreset.DAY;
        }
        if (enabled) apply();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static KalanSkyPreset getPreset() {
        return preset;
    }

    public static void setEnabled(boolean value) {
        if (enabled == value) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (value && client.world != null) {
            trackedWorld = client.world;
            originalTimeOfDay = trackedWorld.getTimeOfDay();
        } else if (!value && trackedWorld != null && client.world == trackedWorld) {
            trackedWorld.setTime(trackedWorld.getTime(), originalTimeOfDay, true);
        }
        enabled = value;
        KalanConfig.get().setCustomSkyEnabled(value);
        if (enabled && client.world != null) {
            apply();
        }
    }

    public static void setPreset(KalanSkyPreset value) {
        if (value == null) return;
        preset = value;
        KalanConfig.get().setCustomSkyPreset(value.name());
        if (enabled) apply();
    }

    private static void apply() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            client.world.setTime(client.world.getTime(), preset.getTime(), false);
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }
}
