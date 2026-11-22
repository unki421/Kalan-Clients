package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class KalanCustomCrosshair {
    private static final int[] COLORS = {
            0xFFB56BFF, 0xFFFFFFFF, 0xFFFF6B8B, 0xFF6BFFD1, 0xFFFFD166, 0xFF6BA8FF
    };
    private static boolean initialized;

    private KalanCustomCrosshair() {}

    public static void initialize() {
        if (initialized) return;
        HudLayerRegistrationCallback.EVENT.register(drawer ->
                drawer.attachLayerAfter(IdentifiedLayer.CROSSHAIR,
                        Identifier.of(KalanClient.MOD_ID, "custom_crosshair"),
                        KalanCustomCrosshair::render));
        initialized = true;
    }

    public static boolean isEnabled() { return KalanConfig.get().isCustomCrosshairEnabled(); }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setCustomCrosshairEnabled(!config.isCustomCrosshairEnabled());
    }

    public static boolean shouldReplaceVanilla() { return isEnabled(); }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!isEnabled() || client == null || client.player == null || client.world == null
                || client.options.hudHidden || client.currentScreen != null || client.player.isSpectator()) return;

        KalanConfig config = KalanConfig.get();
        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        int size = config.getCustomCrosshairSize();
        int gap = config.getCustomCrosshairGap();
        int thickness = config.getCustomCrosshairThickness();
        int color = config.getCustomCrosshairColor();

        drawBar(context, cx - gap - size / 2, cy, size, thickness, color);
        drawBar(context, cx + gap + size / 2, cy, size, thickness, color);
        drawBar(context, cx, cy - gap - size / 2, thickness, size, color);
        drawBar(context, cx, cy + gap + size / 2, thickness, size, color);
    }

    private static void drawBar(DrawContext context, int cx, int cy, int width, int height, int color) {
        int left = cx - width / 2;
        int top = cy - height / 2;
        context.fill(left - 1, top - 1, left + width + 1, top + height + 1, 0xCC080611);
        context.fill(left, top, left + width, top + height, color);
    }

    public static void cycleColor() {
        KalanConfig config = KalanConfig.get();
        int current = config.getCustomCrosshairColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                config.setCustomCrosshairColor(COLORS[(i + 1) % COLORS.length]);
                return;
            }
        }
        config.setCustomCrosshairColor(COLORS[0]);
    }

    public static void adjustSize(int delta) {
        KalanConfig c = KalanConfig.get(); c.setCustomCrosshairSize(c.getCustomCrosshairSize() + delta);
    }
    public static void adjustGap(int delta) {
        KalanConfig c = KalanConfig.get(); c.setCustomCrosshairGap(c.getCustomCrosshairGap() + delta);
    }
    public static void adjustThickness(int delta) {
        KalanConfig c = KalanConfig.get(); c.setCustomCrosshairThickness(c.getCustomCrosshairThickness() + delta);
    }
}
