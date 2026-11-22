package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class KalanChinaHat {
    private static boolean initialized;
    private static boolean enabled;

    private static final int[] COLORS = {
            0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5599FF, 0xFFBB66FF, 0xFFFFFFFF
    };

    private KalanChinaHat() {
    }

    public static void initialize() {
        if (initialized) return;
        enabled = KalanConfig.get().isChinaHatEnabled();
        WorldRenderEvents.AFTER_ENTITIES.register(KalanChinaHat::render);
        initialized = true;
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!enabled || client.world == null || context.matrixStack() == null
                || context.consumers() == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        Vec3d camera = context.camera().getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);
        int color = KalanConfig.get().getChinaHatColor();
        float size = KalanConfig.get().getChinaHatSize();
        float height = KalanConfig.get().getChinaHatHeight();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player.isRemoved() || !player.isAlive()) continue;
            Vec3d pos = player.getLerpedPos(tickDelta).subtract(camera);
            matrices.push();
            matrices.translate(pos.x, pos.y + 1.78 + height, pos.z);
            drawHat(vertices, matrices.peek().getPositionMatrix(), color, size);
            matrices.pop();
        }
    }

    private static void drawHat(VertexConsumer out, Matrix4f matrix, int color, float scale) {
        float brimRadius = 0.38f * scale;
        float coneRadius = 0.19f * scale;
        float coneHeight = 0.42f * scale;
        int segments = 32;

        // Широкие поля шляпы
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            float x0 = (float) Math.cos(a0) * brimRadius;
            float z0 = (float) Math.sin(a0) * brimRadius;
            float x1 = (float) Math.cos(a1) * brimRadius;
            float z1 = (float) Math.sin(a1) * brimRadius;
            float ix0 = (float) Math.cos(a0) * coneRadius;
            float iz0 = (float) Math.sin(a0) * coneRadius;
            float ix1 = (float) Math.cos(a1) * coneRadius;
            float iz1 = (float) Math.sin(a1) * coneRadius;
            quad(out, matrix, color, x0, 0, z0, x1, 0, z1, ix1, 0, iz1, ix0, 0, iz0);
            quad(out, matrix, color, ix0, 0, iz0, ix1, 0, iz1,
                    (float) (Math.cos(a1) * 0.001), 0.001f, (float) (Math.sin(a1) * 0.001),
                    (float) (Math.cos(a0) * 0.001), 0.001f, (float) (Math.sin(a0) * 0.001));
        }

        // Коническая верхушка
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            float x0 = (float) Math.cos(a0) * coneRadius;
            float z0 = (float) Math.sin(a0) * coneRadius;
            float x1 = (float) Math.cos(a1) * coneRadius;
            float z1 = (float) Math.sin(a1) * coneRadius;
            quad(out, matrix, color, x0, 0, z0, x1, 0, z1, 0, coneHeight, 0, 0, coneHeight, 0);
        }
    }

    private static void quad(VertexConsumer out, Matrix4f matrix, int argb,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        out.vertex(matrix, x0, y0, z0).color(r, g, b, a);
        out.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        out.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        out.vertex(matrix, x3, y3, z3).color(r, g, b, a);
    }

    public static void reloadFromConfig() {
        enabled = KalanConfig.get().isChinaHatEnabled();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        KalanConfig.get().setChinaHatEnabled(value);
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static void cycleColor() {
        int current = KalanConfig.get().getChinaHatColor();
        int next = 0;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                next = COLORS[(i + 1) % COLORS.length];
                break;
            }
        }
        if (next == 0) next = COLORS[0];
        KalanConfig.get().setChinaHatColor(next);
    }

    public static void adjustSize(float amount) {
        KalanConfig config = KalanConfig.get();
        config.setChinaHatSize(config.getChinaHatSize() + amount);
    }

    public static void adjustHeight(float amount) {
        KalanConfig config = KalanConfig.get();
        config.setChinaHatHeight(config.getChinaHatHeight() + amount);
    }
}
