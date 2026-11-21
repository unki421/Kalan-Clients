package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    private static final int TILE = 0xFF241B30;
    private static final int TILE_ACTIVE = 0xFF593777;

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

        renderWatermark(client, context);
        renderCoordinates(client, context);
        renderKeystrokes(client, context);
        renderMouse(client, context);
        renderPotions(client, context);
    }

    private static void renderWatermark(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.WATERMARK);
        if (element == null || !element.isVisible()) {
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

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.fill(9, 8, 12, 26, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(
                client.textRenderer, "KALAN CLIENT", 18, 7,
                withOpacity(ACCENT, opacity)
        );
        context.drawTextWithShadow(
                client.textRenderer, playerText, 12, 20,
                withOpacity(WHITE, opacity)
        );
        context.fill(10, 32, width - 10, 33, withOpacity(DIVIDER, opacity));

        context.drawTextWithShadow(
                client.textRenderer, fpsText, 12, 39,
                withOpacity(WHITE, opacity)
        );

        int pingX = 12 + client.textRenderer.getWidth(fpsText) + 14;
        context.drawTextWithShadow(
                client.textRenderer, pingText, pingX, 39,
                withOpacity(MUTED, opacity)
        );

        int timeX = pingX + client.textRenderer.getWidth(pingText) + 14;
        context.drawTextWithShadow(
                client.textRenderer, timeText, timeX, 39,
                withOpacity(WHITE, opacity)
        );

        context.getMatrices().pop();
    }

    private static void renderCoordinates(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.COORDINATES);
        if (element == null || !element.isVisible()) {
            return;
        }

        String coordinates = "XYZ  " + client.player.getBlockX()
                + "  " + client.player.getBlockY()
                + "  " + client.player.getBlockZ();
        String direction = "FACING  "
                + client.player.getHorizontalFacing().getName().toUpperCase();

        int width = Math.max(
                126,
                Math.max(
                        client.textRenderer.getWidth(coordinates),
                        client.textRenderer.getWidth(direction)
                ) + 22
        );
        int height = 38;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, coordinates, 10, 7,
                withOpacity(WHITE, element.getOpacity())
        );
        context.drawTextWithShadow(
                client.textRenderer, direction, 10, 21,
                withOpacity(MUTED, element.getOpacity())
        );

        context.getMatrices().pop();
    }

    private static void renderKeystrokes(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.KEYSTROKES);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 62;
        int height = 62;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawKey(context, client, "W", 22, 5, client.options.forwardKey.isPressed(), element.getOpacity());
        drawKey(context, client, "A", 5, 23, client.options.leftKey.isPressed(), element.getOpacity());
        drawKey(context, client, "S", 22, 23, client.options.backKey.isPressed(), element.getOpacity());
        drawKey(context, client, "D", 39, 23, client.options.rightKey.isPressed(), element.getOpacity());
        drawKey(context, client, "JUMP", 5, 42, client.options.jumpKey.isPressed(), element.getOpacity(), 52);

        context.getMatrices().pop();
    }

    private static void drawKey(
            DrawContext context,
            MinecraftClient client,
            String label,
            int x,
            int y,
            boolean pressed,
            float opacity
    ) {
        drawKey(context, client, label, x, y, pressed, opacity, 16);
    }

    private static void drawKey(
            DrawContext context,
            MinecraftClient client,
            String label,
            int x,
            int y,
            boolean pressed,
            float opacity,
            int width
    ) {
        int color = pressed ? TILE_ACTIVE : TILE;
        context.fill(x, y, x + width, y + 15, withOpacity(color, opacity));
        int textWidth = client.textRenderer.getWidth(label);
        context.drawTextWithShadow(
                client.textRenderer,
                label,
                x + (width - textWidth) / 2,
                y + 4,
                withOpacity(pressed ? WHITE : MUTED, opacity)
        );
    }

    private static void renderMouse(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.MOUSE);
        if (element == null || !element.isVisible()) {
            return;
        }

        boolean leftPressed = GLFW.glfwGetMouseButton(
                client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;
        boolean rightPressed = GLFW.glfwGetMouseButton(
                client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;

        int width = 96;
        int height = 31;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawMouseButton(context, client, "LMB", 7, leftPressed, element.getOpacity());
        drawMouseButton(context, client, "RMB", 51, rightPressed, element.getOpacity());

        context.getMatrices().pop();
    }

    private static void drawMouseButton(
            DrawContext context,
            MinecraftClient client,
            String label,
            int x,
            boolean pressed,
            float opacity
    ) {
        context.fill(
                x, 7, x + 38, 24,
                withOpacity(pressed ? TILE_ACTIVE : TILE, opacity)
        );
        int textWidth = client.textRenderer.getWidth(label);
        context.drawTextWithShadow(
                client.textRenderer,
                label,
                x + (38 - textWidth) / 2,
                12,
                withOpacity(pressed ? WHITE : MUTED, opacity)
        );
    }

    private static void renderPotions(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.POTIONS);
        if (element == null || !element.isVisible()) {
            return;
        }

        List<String> lines = new ArrayList<>();
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            String amplifier = getAmplifierText(effect.getAmplifier());
            String duration = formatDuration(effect.getDuration());
            lines.add(name + amplifier + "  " + duration);
        }

        if (lines.isEmpty()) {
            lines.add("Нет активных эффектов");
        }

        int visibleLines = Math.min(lines.size(), 6);
        int width = 150;
        for (int i = 0; i < visibleLines; i++) {
            width = Math.max(width, Math.min(
                    220,
                    client.textRenderer.getWidth(lines.get(i)) + 20
            ));
        }

        int height = 19 + visibleLines * 12;
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, "POTIONS", 9, 5,
                withOpacity(ACCENT, element.getOpacity())
        );

        for (int i = 0; i < visibleLines; i++) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    lines.get(i),
                    9,
                    17 + i * 12,
                    withOpacity(WHITE, element.getOpacity())
            );
        }

        context.getMatrices().pop();
    }

    private static String getAmplifierText(int amplifier) {
        String[] levels = {"", " II", " III", " IV", " V", " VI"};
        if (amplifier >= 0 && amplifier < levels.length) {
            return amplifier == 0 ? " I" : levels[amplifier];
        }
        return " " + (amplifier + 1);
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) {
            return "∞";
        }

        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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

    private static void prepareElement(
            KalanHudElement element,
            DrawContext context,
            int width,
            int height
    ) {
        element.setSize(width, height);
        element.clampToScreen(
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight()
        );
    }

    private static void drawPanel(
            DrawContext context,
            KalanHudElement element,
            int width,
            int height
    ) {
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawRoundedPanel(
                context, 0, 0, width, height, 6,
                withOpacity(OUTLINE, opacity)
        );
        drawRoundedPanel(
                context, 1, 1, width - 2, height - 2, 5,
                withOpacity(BACKGROUND, opacity)
        );

        context.getMatrices().pop();
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
