package dev.kalan.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class KalanJumpCircle {
    private static final int[] COLORS = {
            0xFFB56BFF, 0xFF55D9FF, 0xFFFF6B9D, 0xFF72E6A6,
            0xFFFFC76B, 0xFFFFFFFF
    };
    private static final int DURATION_TICKS = 14;
    private static boolean initialized;
    private static boolean wasOnGround;
    private static int age = DURATION_TICKS;
    private static Vec3d origin = Vec3d.ZERO;

    private KalanJumpCircle() {}

    public static void initialize() {
        if (initialized) return;
        ClientTickEvents.END_CLIENT_TICK.register(KalanJumpCircle::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(KalanJumpCircle::render);
        initialized = true;
    }

    private static void tick(MinecraftClient client) {
        if (!isEnabled() || client.player == null || client.world == null) {
            wasOnGround = false;
            age = DURATION_TICKS;
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player.isOnGround()) {
            wasOnGround = true;
        } else {
            if (wasOnGround && player.getVelocity().y > 0.08) {
                origin = player.getPos();
                age = 0;
            }
            wasOnGround = false;
        }

        if (age < DURATION_TICKS) age++;
    }

    private static void render(WorldRenderContext context) {
        if (!isEnabled() || age >= DURATION_TICKS || context.matrixStack() == null || context.consumers() == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        float progress = Math.min(1.0f, (float) age / DURATION_TICKS);
        float radius = (0.22f + progress * 1.15f) * KalanConfig.get().getJumpCircleSize();
        float thickness = 0.025f * (1.0f - progress * 0.35f);
        int alpha = Math.max(0, Math.round(220.0f * (1.0f - progress)));
        int color = KalanConfig.get().getJumpCircleColor();
        int argb = (color & 0x00FFFFFF) | (alpha << 24);

        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(origin.x - camera.x, origin.y - camera.y + 0.035, origin.z - camera.z);
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        drawRing(vertices, matrices.peek().getPositionMatrix(), argb, radius, thickness);
        matrices.pop();
    }

    private static void drawRing(VertexConsumer out, Matrix4f matrix, int color, float radius, float thickness) {
        int a = (color >>> 24) & 255;
        int r = (color >>> 16) & 255;
        int g = (color >>> 8) & 255;
        int b = color & 255;
        int segments = 48;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            float x0 = (float) Math.cos(a0);
            float z0 = (float) Math.sin(a0);
            float x1 = (float) Math.cos(a1);
            float z1 = (float) Math.sin(a1);
            out.vertex(matrix, x0 * (radius - thickness), 0, z0 * (radius - thickness)).color(r, g, b, a);
            out.vertex(matrix, x0 * (radius + thickness), 0, z0 * (radius + thickness)).color(r, g, b, a);
            out.vertex(matrix, x1 * (radius + thickness), 0, z1 * (radius + thickness)).color(r, g, b, a);
            out.vertex(matrix, x1 * (radius - thickness), 0, z1 * (radius - thickness)).color(r, g, b, a);
        }
    }

    public static boolean isEnabled() {
        return KalanConfig.get().isJumpCircleEnabled();
    }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setJumpCircleEnabled(!config.isJumpCircleEnabled());
        if (!config.isJumpCircleEnabled()) age = DURATION_TICKS;
    }

    public static void cycleColor() {
        KalanConfig config = KalanConfig.get();
        int current = config.getJumpCircleColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                config.setJumpCircleColor(COLORS[(i + 1) % COLORS.length]);
                return;
            }
        }
        config.setJumpCircleColor(COLORS[0]);
    }

    public static void adjustSize(float amount) {
        KalanConfig config = KalanConfig.get();
        config.setJumpCircleSize(config.getJumpCircleSize() + amount);
    }
}
