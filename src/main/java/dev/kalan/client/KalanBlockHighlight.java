package dev.kalan.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class KalanBlockHighlight {
    private static final int[] COLORS = {
            0x8855AAFF, 0x88FF5555, 0x8855FF88, 0x88FFCC55,
            0x88CC77FF, 0x88FFFFFF
    };

    private static boolean initialized;

    private KalanBlockHighlight() {
    }

    public static void initialize() {
        if (initialized) return;
        WorldRenderEvents.AFTER_ENTITIES.register(KalanBlockHighlight::render);
        initialized = true;
    }

    private static void render(WorldRenderContext context) {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || context.matrixStack() == null || context.consumers() == null) return;
        if (!(client.crosshairTarget instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());
        drawBlock(vertices, matrices.peek().getPositionMatrix(), KalanConfig.get().getBlockHighlightColor());
        matrices.pop();
    }

    private static void drawBlock(VertexConsumer out, Matrix4f matrix, int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        float e = 0.002f;

        face(out, matrix, r, g, b, a, -e, -e, -e, 1 + e, -e, -e, 1 + e, -e, 1 + e, -e, -e, 1 + e);
        face(out, matrix, r, g, b, a, -e, 1 + e, -e, -e, 1 + e, 1 + e, 1 + e, 1 + e, 1 + e, 1 + e, 1 + e, -e);
        face(out, matrix, r, g, b, a, -e, -e, -e, -e, -e, 1 + e, -e, 1 + e, 1 + e, -e, 1 + e, -e);
        face(out, matrix, r, g, b, a, 1 + e, -e, -e, 1 + e, 1 + e, -e, 1 + e, 1 + e, 1 + e, 1 + e, -e, 1 + e);
        face(out, matrix, r, g, b, a, -e, -e, -e, -e, 1 + e, -e, 1 + e, 1 + e, -e, 1 + e, -e, -e);
        face(out, matrix, r, g, b, a, -e, -e, 1 + e, 1 + e, -e, 1 + e, 1 + e, 1 + e, 1 + e, -e, 1 + e, 1 + e);
    }

    private static void face(VertexConsumer out, Matrix4f matrix, int r, int g, int b, int a,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        out.vertex(matrix, x0, y0, z0).color(r, g, b, a);
        out.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        out.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        out.vertex(matrix, x3, y3, z3).color(r, g, b, a);
    }

    public static boolean isEnabled() {
        return KalanConfig.get().isBlockHighlightEnabled();
    }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setBlockHighlightEnabled(!config.isBlockHighlightEnabled());
    }

    public static void cycleColor() {
        KalanConfig config = KalanConfig.get();
        int current = config.getBlockHighlightColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                config.setBlockHighlightColor(COLORS[(i + 1) % COLORS.length]);
                return;
            }
        }
        config.setBlockHighlightColor(COLORS[0]);
    }
}
