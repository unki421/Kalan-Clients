package dev.kalan.client;

import java.util.ArrayDeque;
import java.util.Iterator;
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

public final class KalanPlayerTrails {
    private static final int[] COLORS = {
            0xFFB56BFF, 0xFF55D9FF, 0xFFFF6B9D, 0xFF72E6A6,
            0xFFFFC76B, 0xFFFFFFFF
    };
    private static final int MAX_AGE = 18;
    private static final ArrayDeque<TrailPoint> POINTS = new ArrayDeque<>();
    private static boolean initialized;
    private static Vec3d lastPosition;

    private KalanPlayerTrails() {}

    public static void initialize() {
        if (initialized) return;
        ClientTickEvents.END_CLIENT_TICK.register(KalanPlayerTrails::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(KalanPlayerTrails::render);
        initialized = true;
    }

    private static void tick(MinecraftClient client) {
        if (!isEnabled() || client.player == null || client.world == null) {
            POINTS.clear();
            lastPosition = null;
            return;
        }

        ClientPlayerEntity player = client.player;
        Vec3d position = player.getLerpedPos(1.0f);
        if (lastPosition == null || position.squaredDistanceTo(lastPosition) > 0.0009) {
            POINTS.addFirst(new TrailPoint(position));
            lastPosition = position;
        }

        Iterator<TrailPoint> iterator = POINTS.iterator();
        while (iterator.hasNext()) {
            TrailPoint point = iterator.next();
            point.age++;
            if (point.age > MAX_AGE) iterator.remove();
        }
        while (POINTS.size() > MAX_AGE) POINTS.removeLast();
    }

    private static void render(WorldRenderContext context) {
        if (!isEnabled() || POINTS.isEmpty() || context.matrixStack() == null || context.consumers() == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        int color = KalanConfig.get().getPlayerTrailsColor();

        for (TrailPoint point : POINTS) {
            float life = 1.0f - (float) point.age / MAX_AGE;
            if (life <= 0.0f) continue;
            float half = 0.035f + 0.055f * life;
            int alpha = Math.max(0, Math.min(220, Math.round(220.0f * life)));
            int argb = (color & 0x00FFFFFF) | (alpha << 24);
            Vec3d relative = point.position.subtract(camera);
            matrices.push();
            matrices.translate(relative.x, relative.y + 0.04, relative.z);
            drawDiamond(vertices, matrices.peek().getPositionMatrix(), argb, half);
            matrices.pop();
        }
    }

    private static void drawDiamond(VertexConsumer out, Matrix4f matrix, int color, float radius) {
        int a = (color >>> 24) & 255;
        int r = (color >>> 16) & 255;
        int g = (color >>> 8) & 255;
        int b = color & 255;
        out.vertex(matrix, 0, 0, -radius).color(r, g, b, a);
        out.vertex(matrix, radius, 0, 0).color(r, g, b, a);
        out.vertex(matrix, 0, 0, radius).color(r, g, b, a);
        out.vertex(matrix, -radius, 0, 0).color(r, g, b, a);
    }

    public static boolean isEnabled() {
        return KalanConfig.get().isPlayerTrailsEnabled();
    }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setPlayerTrailsEnabled(!config.isPlayerTrailsEnabled());
        if (!config.isPlayerTrailsEnabled()) {
            POINTS.clear();
            lastPosition = null;
        }
    }

    public static void cycleColor() {
        KalanConfig config = KalanConfig.get();
        int current = config.getPlayerTrailsColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                config.setPlayerTrailsColor(COLORS[(i + 1) % COLORS.length]);
                return;
            }
        }
        config.setPlayerTrailsColor(COLORS[0]);
    }

    private static final class TrailPoint {
        private final Vec3d position;
        private int age;
        private TrailPoint(Vec3d position) { this.position = position; }
    }
}
