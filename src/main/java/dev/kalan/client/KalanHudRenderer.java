package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class KalanHudRenderer {

    private static final Identifier LAYER_ID =
            Identifier.of(KalanClient.MOD_ID, "watermark_hud");

    private static final DateTimeFormatter CLOCK_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final int ACCENT = 0xFFB56BFF;
    private static final int BACKGROUND = 0xD90D0A16;
    private static final int OUTLINE = 0xFF70439B;
    private static final int WHITE = 0xFFF4EDFF;
    private static final int MUTED = 0xFFB8A9C9;
    private static final int DIVIDER = 0xFF392747;

    private static boolean initialized;

    private KalanHudRenderer() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerBefore(
                        IdentifiedLayer.CHAT,
                        LAYER_ID,
                        KalanHudRenderer::render
                )
        );

        initialized = true;
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null
                || client.options.hudHidden
                || client.player == null
                || client.world == null) {
            return;
        }

        KalanHudElement watermark =
                KalanHudRegistry.find(KalanHudPanel.WATERMARK);

        if (watermark == null || !watermark.isVisible()) {
            return;
        }

        String username = client.getSession().getUsername();
        if (username == null || username.isBlank()) {
            username = client.player.getName().getString();
        }

        String fpsText = "FPS " + client.getCurrentFps();
        String pingText = "PING " + getPingText(client);
        String timeText = "TIME " + LocalTime.now().format(CLOCK_FORMAT);
        String playerText = "PLAYER: " + username;
        String metricsText = fpsText + "    " + pingText + "    " + timeText;

        int width = Math.max(
                190,
                Math.max(
                        client.textRenderer.getWidth(playerText),
                        client.textRenderer.getWidth(metricsText)
                ) + 24
        );
        int height = 52;

        watermark.setSize(width, height);
        watermark.clampToScreen(
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight()
        );

        float scale = watermark.getScale();
        float opacity = watermark.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(watermark.getX(), watermark.getY(), 0.0);
        context.getMatrices().scale(scale, scale, 1.0f);

        drawRoundedPanel(
                context,
                0,
                0,
                width,
                height,
                7,
                withOpacity(OUTLINE, opacity)
        );

        drawRoundedPanel(
                context,
                1,
                1,
                width - 2,
                height - 2,
                6,
                withOpacity(BACKGROUND, opacity)
        );

        context.fill(
                9,
                8,
                12,
                26,
                withOpacity(ACCENT, opacity)
        );

        context.drawTextWithShadow(
                client.textRenderer,
                "KALAN CLIENT",
                18,
                7,
                withOpacity(ACCENT, opacity)
        );

        context.drawTextWithShadow(
                client.textRenderer,
                playerText,
                12,
                20,
                withOpacity(WHITE, opacity)
        );

        context.fill(
                10,
                32,
                width - 10,
                33,
                withOpacity(DIVIDER, opacity)
        );

        context.drawTextWithShadow(
                client.textRenderer,
                fpsText,
                12,
                39,
                withOpacity(WHITE, opacity)
        );

        int pingX = 12 + client.textRenderer.getWidth(fpsText) + 14;
        context.drawTextWithShadow(
                client.textRenderer,
                pingText,
                pingX,
                39,
                withOpacity(MUTED, opacity)
        );

        int timeX = pingX + client.textRenderer.getWidth(pingText) + 14;
        context.drawTextWithShadow(
                client.textRenderer,
                timeText,
                timeX,
                39,
                withOpacity(WHITE, opacity)
        );

        context.getMatrices().pop();
    }

    private static String getPingText(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) {
            return "—";
        }

        PlayerListEntry entry = client.getNetworkHandler()
                .getPlayerListEntry(client.player.getUuid());

        if (entry == null) {
            return client.isInSingleplayer() ? "LOCAL" : "—";
        }

        return entry.getLatency() + " ms";
    }

    private static void drawRoundedPanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int radius,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int safeRadius = Math.max(1, Math.min(radius, Math.min(width, height) / 2));

        context.fill(
                x + safeRadius,
                y,
                x + width - safeRadius,
                y + height,
                color
        );

        for (int row = 0; row < safeRadius; row++) {
            double distance = safeRadius - row - 0.5;
            int inset = safeRadius - (int) Math.floor(
                    Math.sqrt(safeRadius * safeRadius - distance * distance)
            );

            context.fill(
                    x + inset,
                    y + row,
                    x + width - inset,
                    y + row + 1,
                    color
            );

            context.fill(
                    x + inset,
                    y + height - row - 1,
                    x + width - inset,
                    y + height - row,
                    color
            );
        }
    }

    private static int withOpacity(int color, float opacity) {
        int originalAlpha = (color >>> 24) & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round(originalAlpha * opacity)));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
