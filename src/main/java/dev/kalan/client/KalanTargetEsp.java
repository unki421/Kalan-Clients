package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class KalanTargetEsp {
    private static final int[] COLORS = {
            0xFFB56BFF, 0xFFFFFFFF, 0xFFFF6B8B, 0xFF6BFFD1,
            0xFFFFD166, 0xFF6BA8FF
    };
    private static boolean initialized;

    private KalanTargetEsp() {}

    public static void initialize() {
        if (initialized) return;
        WorldRenderEvents.AFTER_ENTITIES.register(KalanTargetEsp::render);
        initialized = true;
    }

    private static void render(WorldRenderContext context) {
        if (!isEnabled() || context.matrixStack() == null || context.consumers() == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.currentScreen != null
                || !(client.crosshairTarget instanceof EntityHitResult hit)) return;

        Entity target = hit.getEntity();
        if (target == client.player || target.isRemoved() || !target.isAlive()) return;

        float tickDelta = context.tickCounter().getTickDelta(false);
        Vec3d camera = context.camera().getPos();
        Vec3d pos = target.getLerpedPos(tickDelta).subtract(camera);
        Box bounds = target.getBoundingBox();
        float radius = (float) Math.max(0.42, Math.max((bounds.maxX - bounds.minX), (bounds.maxZ - bounds.minZ)) * 0.72)
                * KalanConfig.get().getTargetEspSize();
        float height = (float) (bounds.maxY - bounds.minY);
        float phase = (System.nanoTime() / 1_000_000_000.0f)
                * KalanConfig.get().getTargetEspSpeed() * 1.7f;
        int color = KalanConfig.get().getTargetEspColor();

        MatrixStack matrices = context.matrixStack();
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        matrices.push();
        matrices.translate(pos.x, pos.y, pos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        switch (KalanConfig.get().getTargetEspStyle()) {
            case 1 -> drawDiamond(vertices, matrix, color, radius, phase, 0.08f);
            case 2 -> {
                drawRing(vertices, matrix, color, radius, phase, 0.06f, 0.04f);
                drawRing(vertices, matrix, color, radius * 0.82f, -phase * 1.25f,
                        Math.max(0.15f, height * 0.58f), 0.035f);
                drawOrbitDots(vertices, matrix, color, radius, phase, Math.max(0.2f, height * 0.58f));
            }
            default -> drawRing(vertices, matrix, color, radius, phase, 0.06f, 0.045f);
        }
        matrices.pop();
    }

    private static void drawRing(VertexConsumer out, Matrix4f matrix, int color, float radius,
                                 float phase, float y, float thickness) {
        int segments = 48;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments + phase;
            double a1 = Math.PI * 2.0 * (i + 1) / segments + phase;
            float x0 = (float) Math.cos(a0) * radius;
            float z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float nx0 = (float) Math.cos(a0) * (radius - thickness);
            float nz0 = (float) Math.sin(a0) * (radius - thickness);
            float nx1 = (float) Math.cos(a1) * (radius - thickness);
            float nz1 = (float) Math.sin(a1) * (radius - thickness);
            quad(out, matrix, color, x0, y, z0, x1, y, z1, nx1, y, nz1, nx0, y, nz0);
        }
    }

    private static void drawDiamond(VertexConsumer out, Matrix4f matrix, int color,
                                    float radius, float phase, float y) {
        float[][] points = new float[4][2];
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5 * i + phase;
            points[i][0] = (float) Math.cos(angle) * radius;
            points[i][1] = (float) Math.sin(angle) * radius;
        }
        for (int i = 0; i < 4; i++) {
            float[] a = points[i];
            float[] b = points[(i + 1) % 4];
            float dx = b[0] - a[0], dz = b[1] - a[1];
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            float ox = -dz / length * 0.035f;
            float oz = dx / length * 0.035f;
            quad(out, matrix, color,
                    a[0] + ox, y, a[1] + oz, b[0] + ox, y, b[1] + oz,
                    b[0] - ox, y, b[1] - oz, a[0] - ox, y, a[1] - oz);
        }
        drawRing(out, matrix, color, radius * 0.72f, -phase * 0.6f, y + 0.025f, 0.025f);
    }

    private static void drawOrbitDots(VertexConsumer out, Matrix4f matrix, int color,
                                      float radius, float phase, float y) {
        for (int i = 0; i < 3; i++) {
            double angle = phase * 1.4 + Math.PI * 2.0 * i / 3.0;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float s = 0.075f;
            quad(out, matrix, color, x - s, y - s, z, x + s, y - s, z,
                    x + s, y + s, z, x - s, y + s, z);
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

    public static boolean isEnabled() { return KalanConfig.get().isTargetEspEnabled(); }
    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setTargetEspEnabled(!config.isTargetEspEnabled());
    }
    public static void cycleStyle() {
        KalanConfig c = KalanConfig.get();
        c.setTargetEspStyle((c.getTargetEspStyle() + 1) % 3);
    }
    public static void cycleColor() {
        KalanConfig c = KalanConfig.get();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == c.getTargetEspColor()) {
                c.setTargetEspColor(COLORS[(i + 1) % COLORS.length]);
                return;
            }
        }
        c.setTargetEspColor(COLORS[0]);
    }
    public static void adjustSize(float delta) {
        KalanConfig c = KalanConfig.get();
        c.setTargetEspSize(c.getTargetEspSize() + delta);
    }
    public static void adjustSpeed(float delta) {
        KalanConfig c = KalanConfig.get();
        c.setTargetEspSpeed(c.getTargetEspSpeed() + delta);
    }
}
