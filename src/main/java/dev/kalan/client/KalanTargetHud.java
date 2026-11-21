package dev.kalan.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class KalanTargetHud {
    private static final Identifier LAYER_ID = Identifier.of(KalanClient.MOD_ID, "target_hud");
    private static final long DISPLAY_TIME_MS = 15_000L;
    private static final int ACCENT = 0xFF9B62F5;
    private static final int BACKGROUND = 0xF20B0714;
    private static final int OUTLINE = 0xFF8B45D8;
    private static final int WHITE = 0xFFF4EDFF;
    private static final int MUTED = 0xFFB8A9C9;
    private static final int DIVIDER = 0xFF392747;
    private static final int TILE = 0xFF21172D;
    private static LivingEntity target;
    private static long lastHitTime;
    private static boolean initialized;

    private KalanTargetHud() {}

    public static void initialize() {
        if (initialized) return;

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (world.isClient && client.player == player
                    && entity instanceof LivingEntity livingEntity && livingEntity != player) {
                target = livingEntity;
                lastHitTime = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null
                    || (target != null && target.getWorld() != client.world)) {
                target = null;
                lastHitTime = 0L;
            }
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, LAYER_ID, KalanTargetHud::render));
        initialized = true;
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden || client.player == null
                || client.world == null) return;

        long elapsed = System.currentTimeMillis() - lastHitTime;
        boolean activeTarget = target != null && elapsed >= 0L
                && elapsed < DISPLAY_TIME_MS && !target.isRemoved()
                && target.getWorld() == client.world;
        boolean preview = !activeTarget && client.currentScreen instanceof ChatScreen;
        if (!activeTarget && !preview) return;

        LivingEntity displayedTarget = activeTarget ? target : client.player;
        KalanHudElement element = KalanHudRegistry.find(KalanHudPanel.TARGET_HUD);
        if (element == null || !element.isVisible()) return;

        final int width = 140;
        final int height = 72;
        element.setSize(width, height);
        element.clampToScreen(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        float opacity = element.getOpacity();

        context.getMatrices().push();
        context.getMatrices().translate(element.getX(), element.getY(), 0.0);
        context.getMatrices().scale(element.getScale(), element.getScale(), 1.0f);

        drawRoundedPanel(context, 0, 0, width, height, 8, withOpacity(OUTLINE, opacity));
        drawRoundedPanel(context, 1, 1, width - 2, height - 2, 7, withOpacity(BACKGROUND, opacity));
        context.drawTextWithShadow(client.textRenderer, "◈  Target HUD", 8, 4,
                withOpacity(ACCENT, opacity));
        drawTargetHead(context, displayedTarget, 8, 22, opacity);

        String name = trimText(client, displayedTarget.getName().getString(), 65);
        context.drawTextWithShadow(client.textRenderer, name, 47, 23, withOpacity(WHITE, opacity));
        if (preview) {
            context.drawTextWithShadow(client.textRenderer, "PREVIEW", 47, 34,
                    withOpacity(ACCENT, opacity));
        }

        float health = Math.max(0.0f, displayedTarget.getHealth());
        float maxHealth = Math.max(1.0f, displayedTarget.getMaxHealth());
        float healthRatio = Math.min(1.0f, health / maxHealth);
        String healthText = String.format(java.util.Locale.ROOT, "%.1f HP", health);
        context.drawTextWithShadow(client.textRenderer, healthText, 47, preview ? 42 : 38,
                withOpacity(MUTED, opacity));

        int barY = 51;
        context.fill(47, barY, 132, barY + 5, withOpacity(DIVIDER, opacity));
        if (healthRatio > 0.0f) {
            int healthColor = healthRatio > 0.5f ? 0xFF78D69A
                    : healthRatio > 0.25f ? 0xFFFFD18A : 0xFFFF8297;
            context.fill(47, barY, 47 + Math.round(85 * healthRatio), barY + 5,
                    withOpacity(healthColor, opacity));
        }

        float timeLeft = preview ? 1.0f
                : Math.max(0.0f, 1.0f - (float) elapsed / DISPLAY_TIME_MS);
        context.fill(8, 68, width - 8, 70, withOpacity(DIVIDER, opacity));
        context.fill(8, 61, 8 + Math.round((width - 16) * timeLeft), 63,
                withOpacity(ACCENT, opacity));
        context.getMatrices().pop();
    }

    private static String trimText(MinecraftClient client, String text, int maxWidth) {
        if (client.textRenderer.getWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && client.textRenderer.getWidth(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private static void drawTargetHead(DrawContext context, LivingEntity entity, int x, int y, float opacity) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            Identifier skin = player.getSkinTextures().texture();
            context.drawTexture(RenderLayer::getGuiTextured, skin, x, y,
                    8.0f, 8.0f, 32, 32, 8, 8, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, x, y,
                    40.0f, 8.0f, 32, 32, 8, 8, 64, 64);
        } else {
            drawRoundedPanel(context, x, y, 32, 32, 5, withOpacity(TILE, opacity));
            String initial = entity.getName().getString();
            if (!initial.isEmpty()) {
                initial = initial.substring(0, 1).toUpperCase();
                MinecraftClient client = MinecraftClient.getInstance();
                int textWidth = client.textRenderer.getWidth(initial);
                context.drawTextWithShadow(client.textRenderer, initial,
                        x + (32 - textWidth) / 2, y + 12, withOpacity(WHITE, opacity));
            }
        }
    }

    private static void drawRoundedPanel(DrawContext context, int x, int y, int width,
                                         int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int safeRadius = Math.max(1, Math.min(radius, Math.min(width, height) / 2));
        context.fill(x + safeRadius, y, x + width - safeRadius, y + height, color);
        for (int row = 0; row < safeRadius; row++) {
            double distance = safeRadius - row - 0.5;
            int inset = safeRadius - (int) Math.floor(
                    Math.sqrt(safeRadius * safeRadius - distance * distance));
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
            context.fill(x + inset, y + height - row - 1,
                    x + width - inset, y + height - row, color);
        }
    }

    private static int withOpacity(int color, float opacity) {
        int originalAlpha = (color >>> 24) & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round(originalAlpha * opacity)));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
