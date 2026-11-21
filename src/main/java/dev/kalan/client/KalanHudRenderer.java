package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
        renderArmor(client, context);
        renderPlayerStatus(client, context);
        renderArrayList(client, context);
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

        boolean leftPressed = client.options.attackKey.isPressed();
        boolean rightPressed = client.options.useKey.isPressed();

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

    private static void renderArmor(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARMOR);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 112;
        int height = 42;
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, "ARMOR", 8, 4,
                withOpacity(ACCENT, element.getOpacity())
        );

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            int itemX = 8 + i * 25;

            if (stack.isEmpty()) {
                context.fill(
                        itemX + 2, 18, itemX + 18, 34,
                        withOpacity(TILE, element.getOpacity())
                );
                continue;
            }

            context.drawItem(stack, itemX, 16);

            if (stack.isDamageable()) {
                float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                int barColor = remaining > 0.5f ? 0xFF78D69A
                        : remaining > 0.2f ? 0xFFFFD18A : 0xFFFF8297;
                context.fill(itemX + 1, 34, itemX + 20, 37,
                        withOpacity(DIVIDER, element.getOpacity()));
                context.fill(itemX + 1, 34,
                        itemX + 1 + Math.round(19 * remaining), 37,
                        withOpacity(barColor, element.getOpacity()));
            }
        }

        context.getMatrices().pop();
    }

    private static void renderPlayerStatus(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.PLAYER_STATUS);
        if (element == null || !element.isVisible()) {
            return;
        }

        float health = client.player.getHealth();
        float maxHealth = client.player.getMaxHealth();
        int food = client.player.getHungerManager().getFoodLevel();
        int armor = client.player.getArmor();

        String healthText = String.format("HP  %.1f / %.0f", health, maxHealth);
        String foodText = "FOOD  " + food + " / 20";
        String armorText = "ARMOR  " + armor;

        int width = Math.max(
                126,
                Math.max(
                        client.textRenderer.getWidth(healthText),
                        Math.max(
                                client.textRenderer.getWidth(foodText),
                                client.textRenderer.getWidth(armorText)
                        )
                ) + 20
        );
        int height = 54;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, "PLAYER STATUS", 9, 5,
                withOpacity(ACCENT, element.getOpacity())
        );
        context.drawTextWithShadow(
                client.textRenderer, healthText, 9, 18,
                withOpacity(WHITE, element.getOpacity())
        );
        context.drawTextWithShadow(
                client.textRenderer, foodText, 9, 30,
                withOpacity(MUTED, element.getOpacity())
        );
        context.drawTextWithShadow(
                client.textRenderer, armorText, 9, 42,
                withOpacity(MUTED, element.getOpacity())
        );

        context.getMatrices().pop();
    }

    private static void renderArrayList(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARRAY_LIST);
        if (element == null || !element.isVisible()) {
            return;
        }

        List<String> enabled = new ArrayList<>();
        for (KalanHudPanel panel : KalanHudPanel.values()) {
            if (panel == KalanHudPanel.ARRAY_LIST) {
                continue;
            }

            KalanHudElement panelElement = KalanHudRegistry.find(panel);
            if (panelElement != null && panelElement.isVisible()) {
                enabled.add(panel.getTitle());
            }
        }

        enabled.sort(Comparator.comparing(String::toLowerCase));

        int width = 92;
        for (String name : enabled) {
            width = Math.max(width, client.textRenderer.getWidth(name) + 18);
        }

        int height = Math.max(14, enabled.size() * 13 + 4);
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        for (int i = 0; i < enabled.size(); i++) {
            String name = enabled.get(i);
            int textWidth = client.textRenderer.getWidth(name);
            context.fill(
                    width - 3, 3 + i * 13,
                    width - 1, 12 + i * 13,
                    withOpacity(ACCENT, element.getOpacity())
            );
            context.drawTextWithShadow(
                    client.textRenderer,
                    name,
                    width - textWidth - 8,
                    3 + i * 13,
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
