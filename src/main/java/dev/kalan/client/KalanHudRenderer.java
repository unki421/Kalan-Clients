package dev.kalan.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class KalanHudRenderer {

    private static final Identifier LAYER_ID =
            Identifier.of(KalanClient.MOD_ID, "watermark_hud");

    private static final DateTimeFormatter CLOCK_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final int ACCENT = 0xFF9B62F5;
    private static final int BACKGROUND = 0xF20B0714;
    private static final int OUTLINE = 0xFF8B45D8;
    private static final int WHITE = 0xFFF4EDFF;
    private static final int MUTED = 0xFFB8A9C9;
    private static final int DIVIDER = 0xFF392747;
    private static final int TILE = 0xFF21172D;
    private static final int TILE_ACTIVE = 0xFF4C3270;

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
        renderInfo(client, context);
        renderCoordinates(client, context);
        renderKeystrokes(client, context);
        renderMouse(client, context);
        renderPotions(client, context);
        renderArmor(client, context);
        renderTargetHud(client, context);
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

        String playerLine = "Player: " + username;
        String fpsText = "FPS: " + client.getCurrentFps();
        String pingText = "Ping: " + getPingText(client);
        String timeText = "Time: " + LocalTime.now().format(CLOCK_FORMAT);

        int topWidth = Math.max(
                148,
                Math.max(
                        client.textRenderer.getWidth("Kalan Client") + 38,
                        client.textRenderer.getWidth(playerLine) + 38
                )
        );

        int fpsW = client.textRenderer.getWidth(fpsText) + 12;
        int pingW = client.textRenderer.getWidth(pingText) + 12;
        int timeW = client.textRenderer.getWidth(timeText) + 12;
        int rowWidth = fpsW + pingW + timeW + 10;
        int width = Math.max(topWidth, rowWidth);
        int height = 56;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawRoundedPanel(context, 0, 0, topWidth, 32, 8, withOpacity(OUTLINE, opacity));
        drawRoundedPanel(context, 1, 1, topWidth - 2, 30, 7, withOpacity(BACKGROUND, opacity));
        context.drawTextWithShadow(client.textRenderer, "◢", 9, 10, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Kalan Client", 26, 6, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, playerLine, 26, 18, withOpacity(MUTED, opacity));

        int pillY = 36;
        int pillH = 18;
        drawMiniPill(context, client, 0, pillY, fpsW, pillH, fpsText, WHITE, opacity);
        drawMiniPill(context, client, fpsW + 5, pillY, pingW, pillH, pingText, MUTED, opacity);
        drawMiniPill(context, client, fpsW + pingW + 10, pillY, timeW, pillH, timeText, WHITE, opacity);

        context.getMatrices().pop();
    }

    private static void drawMiniPill(
            DrawContext context,
            MinecraftClient client,
            int x,
            int y,
            int w,
            int h,
            String text,
            int color,
            float opacity
    ) {
        drawRoundedPanel(context, x, y, w, h, 6, withOpacity(OUTLINE, opacity));
        drawRoundedPanel(context, x + 1, y + 1, w - 2, h - 2, 5, withOpacity(BACKGROUND, opacity));
        context.drawTextWithShadow(client.textRenderer, text, x + 6, y + 5, withOpacity(color, opacity));
    }

    private static void renderInfo(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.INFO);
        if (element == null || !element.isVisible()) {
            return;
        }

        String line1 = "HP: " + Math.round(client.player.getHealth())
                + " / " + Math.round(client.player.getMaxHealth());
        String line2 = "Food: " + client.player.getHungerManager().getFoodLevel();

        int width = Math.max(110, Math.max(
                client.textRenderer.getWidth(line1),
                client.textRenderer.getWidth(line2)
        ) + 18);
        int height = 34;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(client.textRenderer, line1, 8, 6, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, line2, 8, 19, withOpacity(MUTED, opacity));

        context.getMatrices().pop();
    }

    private static void renderCoordinates(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.COORDINATES);
        if (element == null || !element.isVisible()) {
            return;
        }

        String coordinates = "XYZ: " + client.player.getBlockX()
                + " " + client.player.getBlockY()
                + " " + client.player.getBlockZ();
        String direction = "Facing: "
                + client.player.getHorizontalFacing().getName().toUpperCase();

        int width = Math.max(
                132,
                Math.max(
                        client.textRenderer.getWidth(coordinates),
                        client.textRenderer.getWidth(direction)
                ) + 26
        );
        int height = 36;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(client.textRenderer, "⌖", 8, 7, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, coordinates, 20, 7, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, "➤", 8, 20, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, direction, 20, 20, withOpacity(MUTED, opacity));

        context.getMatrices().pop();
    }

    private static void renderKeystrokes(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.KEYSTROKES);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 96;
        int height = 94;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(client.textRenderer, "⌨ Keystrokes", 8, 5, withOpacity(ACCENT, opacity));
        drawKey(context, client, "W", 39, 20, client.options.forwardKey.isPressed(), opacity, 18);
        drawKey(context, client, "A", 19, 40, client.options.leftKey.isPressed(), opacity, 18);
        drawKey(context, client, "S", 39, 40, client.options.backKey.isPressed(), opacity, 18);
        drawKey(context, client, "D", 59, 40, client.options.rightKey.isPressed(), opacity, 18);
        drawKey(context, client, "LMB", 8, 61, client.options.attackKey.isPressed(), opacity, 38);
        drawKey(context, client, "RMB", 50, 61, client.options.useKey.isPressed(), opacity, 38);

        context.getMatrices().pop();
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
        drawRoundedPanel(context, x, y, width, 16, 4, withOpacity(color, opacity));
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

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawMouseButton(context, client, "LMB", 7, leftPressed, opacity);
        drawMouseButton(context, client, "RMB", 51, rightPressed, opacity);

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
        drawRoundedPanel(context, x, 7, 38, 17, 4,
                withOpacity(pressed ? TILE_ACTIVE : TILE, opacity));
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
        int width = 152;
        for (int i = 0; i < visibleLines; i++) {
            width = Math.max(width, Math.min(
                    220,
                    client.textRenderer.getWidth(lines.get(i)) + 20
            ));
        }

        int height = 24 + visibleLines * 14;
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, "✦ Potions", 9, 5,
                withOpacity(ACCENT, opacity)
        );

        for (int i = 0; i < visibleLines; i++) {
            context.drawTextWithShadow(
                    client.textRenderer,
                    lines.get(i),
                    9,
                    19 + i * 14,
                    withOpacity(WHITE, opacity)
            );
        }

        context.getMatrices().pop();
    }

    private static void renderArmor(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARMOR);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 114;
        int height = 46;
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(
                client.textRenderer, "♢ Armor", 8, 4,
                withOpacity(ACCENT, opacity)
        );

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            int slotX = 8 + i * 25;
            drawRoundedPanel(context, slotX, 16, 20, 20, 4, withOpacity(TILE, opacity));

            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX + 2, 18);

                if (stack.isDamageable()) {
                    float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                    int barColor = remaining > 0.5f ? 0xFF78D69A
                            : remaining > 0.2f ? 0xFFFFD18A : 0xFFFF8297;
                    context.fill(slotX + 2, 37, slotX + 18, 39,
                            withOpacity(DIVIDER, opacity));
                    context.fill(slotX + 2, 37,
                            slotX + 2 + Math.round(16 * remaining), 39,
                            withOpacity(barColor, opacity));
                }
            }
        }

        context.getMatrices().pop();
    }

    private static void renderTargetHud(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.TARGET_HUD);
        if (element == null || !element.isVisible()) {
            return;
        }

        LivingEntity target = null;
        if (client.targetedEntity instanceof LivingEntity living) {
            target = living;
        } else if (client.player.getAttacking() instanceof LivingEntity attacking) {
            target = attacking;
        }

        if (target == null) {
            return;
        }

        String name = target.getName().getString();
        float hp = Math.max(0.0f, target.getHealth());
        String hpText = String.format("%.1f HP", hp);

        int width = Math.max(132, client.textRenderer.getWidth(name) + 48);
        int height = 44;

        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(client.textRenderer, "⚔ Target HUD", 8, 4,
                withOpacity(ACCENT, opacity));
        drawRoundedPanel(context, 8, 15, 22, 22, 4, withOpacity(TILE, opacity));
        context.drawTextWithShadow(client.textRenderer, name, 35, 16,
                withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, hpText, 35, 27,
                withOpacity(MUTED, opacity));

        float maxHp = Math.max(1.0f, target.getMaxHealth());
        float ratio = Math.max(0.0f, Math.min(1.0f, hp / maxHp));
        int barW = width - 43;
        context.fill(35, 37, 35 + barW, 40, withOpacity(DIVIDER, opacity));
        context.fill(35, 37, 35 + Math.round(barW * ratio), 40, withOpacity(ACCENT, opacity));

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

        enabled.sort((a, b) -> Integer.compare(
                client.textRenderer.getWidth(b),
                client.textRenderer.getWidth(a)
        ));

        int maxShown = 5;
        int shownCount = Math.min(enabled.size(), maxShown);
        int extraCount = Math.max(0, enabled.size() - maxShown);
        String moreText = extraCount > 0 ? "+ " + extraCount + " more..." : null;

        int width = 108;
        for (int i = 0; i < shownCount; i++) {
            width = Math.max(width, client.textRenderer.getWidth(enabled.get(i)) + 26);
        }
        if (moreText != null) {
            width = Math.max(width, client.textRenderer.getWidth(moreText) + 18);
        }

        int height = 22 + shownCount * 14 + (moreText != null ? 13 : 0);
        prepareElement(element, context, width, height);
        drawPanel(context, element, width, height);

        float opacity = element.getOpacity();
        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        context.drawTextWithShadow(client.textRenderer, "≣ ArrayList", 8, 5,
                withOpacity(ACCENT, opacity));

        for (int i = 0; i < shownCount; i++) {
            String name = enabled.get(i);
            int rowY = 19 + i * 14;
            context.drawTextWithShadow(client.textRenderer, "◆", 9, rowY,
                    withOpacity(ACCENT, opacity));
            context.drawTextWithShadow(client.textRenderer, name, 20, rowY,
                    withOpacity(WHITE, opacity));
        }

        if (moreText != null) {
            int moreY = 19 + shownCount * 14;
            int moreW = client.textRenderer.getWidth(moreText);
            context.drawTextWithShadow(client.textRenderer, moreText,
                    width - moreW - 8, moreY, withOpacity(MUTED, opacity));
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
                context, 0, 0, width, height, 7,
                withOpacity(OUTLINE, opacity)
        );
        drawRoundedPanel(
                context, 1, 1, width - 2, height - 2, 6,
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
