package dev.kalan.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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

    private static final int GLOW = 0x338B5CF6;
    private static final int BACKGROUND = 0xE6110C1F;
    private static final int OUTLINE = 0xCC7B4AE2;
    private static final int ACCENT = 0xFF9D6BFF;
    private static final int ACCENT_FILL = 0xFF8338EC;
    private static final int WHITE = 0xFFF6F2FF;
    private static final int MUTED = 0xFFA599C2;
    private static final int DIVIDER = 0x556441A5;
    private static final int KEY_BG = 0xEB18122B;
    private static final int KEY_BORDER = 0x99693DC4;
    private static final int KEY_ACTIVE = 0xFF532A94;

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
        renderPotions(client, context);
        renderArmor(client, context);
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

        String fpsText = "FPS: " + client.getCurrentFps();
        String pingText = "Ping: " + getPingText(client);
        String timeText = "Time: " + LocalTime.now().format(CLOCK_FORMAT);

        int fpsW = client.textRenderer.getWidth(fpsText) + 14;
        int pingW = client.textRenderer.getWidth(pingText) + 14;
        int timeW = client.textRenderer.getWidth(timeText) + 14;
        int pillsTotalW = fpsW + pingW + timeW + 8;

        int topCardW = Math.max(
                pillsTotalW,
                Math.max(
                        client.textRenderer.getWidth("Kalan Client") + 40,
                        client.textRenderer.getWidth("Player: " + username) + 40
                )
        );

        int width = Math.max(topCardW, pillsTotalW);
        int height = 55;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        // Верхняя карточка Kalan Client + Player
        drawCard(context, 0, 0, topCardW, 33, 9, opacity);
        drawLogoMark(context, 10, 10, opacity);
        context.drawTextWithShadow(client.textRenderer, "Kalan Client", 30, 6,
                withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, "Player: ", 30, 18,
                withOpacity(MUTED, opacity));
        int labelW = client.textRenderer.getWidth("Player: ");
        context.drawTextWithShadow(client.textRenderer, username, 30 + labelW, 18,
                withOpacity(WHITE, opacity));

        // Нижние 3 капсулы: FPS, Ping, Time
        int pillY = 37;
        int pillH = 18;
        drawCard(context, 0, pillY, fpsW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, fpsText, 7, pillY + 5,
                withOpacity(WHITE, opacity));

        int pingX = fpsW + 4;
        drawCard(context, pingX, pillY, pingW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, pingText, pingX + 7, pillY + 5,
                withOpacity(WHITE, opacity));

        int timeX = pingX + pingW + 4;
        drawCard(context, timeX, pillY, timeW, pillH, 6, opacity);
        context.drawTextWithShadow(client.textRenderer, timeText, timeX + 7, pillY + 5,
                withOpacity(WHITE, opacity));

        context.getMatrices().pop();
    }

    private static void drawLogoMark(DrawContext context, int x, int y, float opacity) {
        context.fill(x, y + 1, x + 14, y + 3, withOpacity(WHITE, opacity));
        context.fill(x + 2, y + 4, x + 13, y + 6, withOpacity(ACCENT, opacity));
        context.fill(x + 4, y + 7, x + 11, y + 9, withOpacity(WHITE, opacity));
        context.fill(x + 6, y + 10, x + 9, y + 13, withOpacity(ACCENT, opacity));
    }

    private static void renderInfo(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.INFO);
        if (element == null || !element.isVisible()) {
            return;
        }

        String line1 = "HP: " + Math.round(client.player.getHealth())
                + "/" + Math.round(client.player.getMaxHealth());
        String line2 = "Hunger: " + client.player.getHungerManager().getFoodLevel();

        int width = Math.max(106, Math.max(
                client.textRenderer.getWidth(line1),
                client.textRenderer.getWidth(line2)
        ) + 20);
        int height = 34;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);
        context.drawTextWithShadow(client.textRenderer, line1, 9, 6, withOpacity(WHITE, opacity));
        context.drawTextWithShadow(client.textRenderer, line2, 9, 19, withOpacity(MUTED, opacity));

        context.getMatrices().pop();
    }

    private static void renderCoordinates(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.COORDINATES);
        if (element == null || !element.isVisible()) {
            return;
        }

        String coordsVal = client.player.getBlockX()
                + " " + client.player.getBlockY()
                + " " + client.player.getBlockZ();
        String facingVal = client.player.getHorizontalFacing().getName().toUpperCase();

        int width = Math.max(
                134,
                Math.max(
                        client.textRenderer.getWidth("XYZ: " + coordsVal),
                        client.textRenderer.getWidth("Facing: " + facingVal)
                ) + 28
        );
        int height = 35;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);

        context.drawTextWithShadow(client.textRenderer, "⌖", 9, 6, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "XYZ: ", 21, 6, withOpacity(MUTED, opacity));
        int xyzLabelW = client.textRenderer.getWidth("XYZ: ");
        context.drawTextWithShadow(client.textRenderer, coordsVal, 21 + xyzLabelW, 6,
                withOpacity(WHITE, opacity));

        context.drawTextWithShadow(client.textRenderer, "➤", 9, 19, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Facing: ", 21, 19, withOpacity(MUTED, opacity));
        int faceLabelW = client.textRenderer.getWidth("Facing: ");
        context.drawTextWithShadow(client.textRenderer, facingVal, 21 + faceLabelW, 19,
                withOpacity(WHITE, opacity));

        context.getMatrices().pop();
    }

    private static void renderKeystrokes(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.KEYSTROKES);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 92;
        int height = 92;

        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "⌨", 8, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Keystrokes", 20, 5, withOpacity(WHITE, opacity));

        drawKeyTile(context, client, "W", 37, 21, 18, 17,
                client.options.forwardKey.isPressed(), opacity);
        drawKeyTile(context, client, "A", 16, 41, 18, 17,
                client.options.leftKey.isPressed(), opacity);
        drawKeyTile(context, client, "S", 37, 41, 18, 17,
                client.options.backKey.isPressed(), opacity);
        drawKeyTile(context, client, "D", 58, 41, 18, 17,
                client.options.rightKey.isPressed(), opacity);

        drawKeyTile(context, client, "LMB", 8, 62, 36, 18,
                client.options.attackKey.isPressed(), opacity);
        drawKeyTile(context, client, "RMB", 48, 62, 36, 18,
                client.options.useKey.isPressed(), opacity);

        context.getMatrices().pop();
    }

    private static void drawKeyTile(
            DrawContext context,
            MinecraftClient client,
            String label,
            int x,
            int y,
            int w,
            int h,
            boolean pressed,
            float opacity
    ) {
        drawRoundedPanel(context, x, y, w, h, 4,
                withOpacity(pressed ? ACCENT : KEY_BORDER, opacity));
        drawRoundedPanel(context, x + 1, y + 1, w - 2, h - 2, 3,
                withOpacity(pressed ? KEY_ACTIVE : KEY_BG, opacity));

        int textW = client.textRenderer.getWidth(label);
        context.drawTextWithShadow(
                client.textRenderer,
                label,
                x + (w - textW) / 2,
                y + (h - client.textRenderer.fontHeight) / 2 + 1,
                withOpacity(pressed ? WHITE : MUTED, opacity)
        );
    }

    private static void renderPotions(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.POTIONS);
        if (element == null || !element.isVisible()) {
            return;
        }

        List<String[]> rows = new ArrayList<>();
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString()
                    + getAmplifierText(effect.getAmplifier());
            String duration = formatDuration(effect.getDuration());
            rows.add(new String[]{name, duration});
        }

        if (rows.isEmpty()) {
            rows.add(new String[]{"Нет эффектов", ""});
        }

        int visible = Math.min(rows.size(), 6);
        int width = 148;
        for (int i = 0; i < visible; i++) {
            int rowW = client.textRenderer.getWidth(rows.get(i)[0])
                    + client.textRenderer.getWidth(rows.get(i)[1]) + 34;
            width = Math.max(width, Math.min(210, rowW));
        }

        int height = 24 + visible * 14;
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "⚗", 9, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Potions", 20, 5, withOpacity(WHITE, opacity));
        c.fillDivider(context, 8, 16, width - 8, opacity);

        for (int i = 0; i < visible; i++) {
            int rowY = 20 + i * 14;
            String name = rows.get(i)[0];
            String time = rows.get(i)[1];

            context.drawTextWithShadow(client.textRenderer, "•", 9, rowY, withOpacity(ACCENT, opacity));
            context.drawTextWithShadow(client.textRenderer, name, 18, rowY, withOpacity(WHITE, opacity));
            if (!time.isEmpty()) {
                int timeW = client.textRenderer.getWidth(time);
                context.drawTextWithShadow(client.textRenderer, time,
                        width - timeW - 9, rowY, withOpacity(MUTED, opacity));
            }
        }

        context.getMatrices().pop();
    }

    private static void renderArmor(MinecraftClient client, DrawContext context) {
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.ARMOR);
        if (element == null || !element.isVisible()) {
            return;
        }

        int width = 108;
        int height = 43;
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 8, opacity);
        context.drawTextWithShadow(client.textRenderer, "🛡", 8, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "Armor", 20, 5, withOpacity(WHITE, opacity));

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            int slotX = 8 + i * 24;
            drawRoundedPanel(context, slotX, 17, 20, 20, 4, withOpacity(KEY_BG, opacity));

            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX + 2, 19);

                if (stack.isDamageable()) {
                    float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                    int barColor = remaining > 0.5f ? 0xFF7CE3A0
                            : remaining > 0.25f ? 0xFFFFD166 : 0xFFFF6B8B;
                    context.fill(slotX + 2, 36, slotX + 18, 38, withOpacity(DIVIDER, opacity));
                    context.fill(slotX + 2, 36,
                            slotX + 2 + Math.round(16 * remaining), 38,
                            withOpacity(barColor, opacity));
                }
            }
        }

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
            width = Math.max(width, client.textRenderer.getWidth(enabled.get(i)) + 28);
        }
        if (moreText != null) {
            width = Math.max(width, client.textRenderer.getWidth(moreText) + 18);
        }

        int height = 24 + shownCount * 14 + (moreText != null ? 13 : 0);
        prepareElement(element, context, width, height);
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawCard(context, 0, 0, width, height, 9, opacity);
        context.drawTextWithShadow(client.textRenderer, "≣", 9, 5, withOpacity(ACCENT, opacity));
        context.drawTextWithShadow(client.textRenderer, "ArrayList", 20, 5, withOpacity(WHITE, opacity));
        c.fillDivider(context, 8, 16, width - 8, opacity);

        for (int i = 0; i < shownCount; i++) {
            String name = enabled.get(i);
            int rowY = 20 + i * 14;
            context.drawTextWithShadow(client.textRenderer, "◆", 9, rowY,
                    withOpacity(ACCENT, opacity));
            context.drawTextWithShadow(client.textRenderer, name, 20, rowY,
                    withOpacity(WHITE, opacity));
        }

        if (moreText != null) {
            int moreY = 20 + shownCount * 14;
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

    private static void drawCard(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int radius,
            float opacity
    ) {
        drawRoundedPanel(context, x - 1, y - 1, width + 2, height + 2,
                radius + 1, withOpacity(GLOW, opacity));
        drawRoundedPanel(context, x, y, width, height,
                radius, withOpacity(OUTLINE, opacity));
        drawRoundedPanel(context, x + 1, y + 1, width - 2, height - 2,
                Math.max(1, radius - 1), withOpacity(BACKGROUND, opacity));
    }

    private static void roundedBar(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        drawRoundedPanel(context, x, y, width, height, height / 2, color);
    }

    private static final class c {
        private static void fillDivider(DrawContext context, int x1, int y, int x2, float opacity) {
            context.fill(x1, y, x2, y + 1, withOpacity(DIVIDER, opacity));
        }
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
