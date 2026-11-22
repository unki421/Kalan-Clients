package dev.kalan.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import org.joml.Matrix4f;

public final class KalanSnow {
    private static final int MAX_PARTICLES = 180;
    private static final List<Snowflake> FLAKES = new ArrayList<>();
    private static final List<BlockPos> SNOWY_TOPS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static boolean initialized;
    private static int cacheTicks;

    private KalanSnow() {}

    public static void initialize() {
        if (initialized) return;
        ClientTickEvents.END_CLIENT_TICK.register(KalanSnow::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(KalanSnow::render);
        initialized = true;
    }

    private static void tick(MinecraftClient client) {
        if (!isEnabled() || client.player == null || client.world == null) {
            FLAKES.clear();
            SNOWY_TOPS.clear();
            cacheTicks = 0;
            return;
        }

        int intensity = KalanConfig.get().getSnowIntensity();
        int targetCount = intensity * 28;
        Vec3d player = client.player.getPos();

        while (FLAKES.size() < targetCount && FLAKES.size() < MAX_PARTICLES) {
            FLAKES.add(createFlake(player));
        }
        while (FLAKES.size() > targetCount) {
            FLAKES.remove(FLAKES.size() - 1);
        }

        for (int i = 0; i < FLAKES.size(); i++) {
            Snowflake flake = FLAKES.get(i);
            flake.x += flake.driftX;
            flake.y -= flake.fallSpeed;
            flake.z += flake.driftZ;
            if (flake.y < player.y - 2.5 || flake.position().squaredDistanceTo(player) > 420.0) {
                FLAKES.set(i, createFlake(player));
            }
        }

        if (++cacheTicks >= 10) {
            rebuildSnowCover(client, player);
            cacheTicks = 0;
        }
    }

    private static Snowflake createFlake(Vec3d player) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double radius = Math.sqrt(RANDOM.nextDouble()) * 12.0;
        double x = player.x + Math.cos(angle) * radius;
        double z = player.z + Math.sin(angle) * radius;
        double y = player.y + 3.0 + RANDOM.nextDouble() * 9.0;
        return new Snowflake(x, y, z,
                (RANDOM.nextDouble() - 0.5) * 0.035,
                0.10 + RANDOM.nextDouble() * 0.10,
                (RANDOM.nextDouble() - 0.5) * 0.035,
                0.025f + RANDOM.nextFloat() * 0.035f);
    }

    private static void rebuildSnowCover(MinecraftClient client, Vec3d player) {
        SNOWY_TOPS.clear();
        BlockPos center = BlockPos.ofFloored(player);
        int radius = 8;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos.Mutable pos = new BlockPos.Mutable(center.getX() + x,
                        center.getY(), center.getZ() + z);
                for (int dy = -3; dy <= 4; dy++) {
                    pos.setY(center.getY() + dy);
                    BlockState state = client.world.getBlockState(pos);
                    if (state.isAir() || !state.isSolidBlock(client.world, pos)) continue;
                    if (!client.world.getBlockState(pos.up()).isAir()) continue;
                    SNOWY_TOPS.add(pos.toImmutable());
                }
            }
        }
    }

    private static void render(WorldRenderContext context) {
        if (!isEnabled() || context.matrixStack() == null || context.consumers() == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugQuads());

        int intensity = KalanConfig.get().getSnowIntensity();
        for (Snowflake flake : FLAKES) {
            Vec3d relative = flake.position().subtract(camera);
            float alpha = 0.55f + intensity * 0.07f;
            int a = MathHelper.clamp(Math.round(alpha * 255.0f), 0, 255);
            matrices.push();
            matrices.translate(relative.x, relative.y, relative.z);
            matrices.multiply(context.camera().getRotation());
            drawFlake(vertices, matrices.peek().getPositionMatrix(), flake.size, a);
            matrices.pop();
        }

        int coverAlpha = 75 + intensity * 22;
        for (BlockPos pos : SNOWY_TOPS) {
            double dx = pos.getX() + 0.5 - client.player.getX();
            double dz = pos.getZ() + 0.5 - client.player.getZ();
            if (dx * dx + dz * dz > 100.0) continue;
            matrices.push();
            matrices.translate(pos.getX() - camera.x, pos.getY() + 1.003 - camera.y, pos.getZ() - camera.z);
            drawSnowPatches(vertices, matrices.peek().getPositionMatrix(), pos, coverAlpha);
            matrices.pop();
        }
    }

    private static void drawFlake(VertexConsumer out, Matrix4f matrix, float size, int alpha) {
        int a = Math.min(255, alpha);
        out.vertex(matrix, -size, -size, 0).color(245, 249, 255, a);
        out.vertex(matrix, size, -size, 0).color(245, 249, 255, a);
        out.vertex(matrix, size, size, 0).color(245, 249, 255, a);
        out.vertex(matrix, -size, size, 0).color(245, 249, 255, a);
    }

    private static void drawSnowPatches(VertexConsumer out, Matrix4f matrix, BlockPos pos, int alpha) {
        long seed = pos.asLong() * 31L + 0x9E3779B97F4A7C15L;
        Random random = new Random(seed);
        for (int i = 0; i < 5; i++) {
            float x = 0.04f + random.nextFloat() * 0.78f;
            float z = 0.04f + random.nextFloat() * 0.78f;
            float w = 0.12f + random.nextFloat() * 0.22f;
            float d = 0.12f + random.nextFloat() * 0.22f;
            int a = MathHelper.clamp(alpha - random.nextInt(28), 0, 255);
            out.vertex(matrix, x, 0, z).color(238, 244, 255, a);
            out.vertex(matrix, x + w, 0, z).color(248, 250, 255, a);
            out.vertex(matrix, x + w, 0, z + d).color(248, 250, 255, a);
            out.vertex(matrix, x, 0, z + d).color(238, 244, 255, a);
        }
    }

    public static boolean isEnabled() {
        return KalanConfig.get().isSnowEnabled();
    }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setSnowEnabled(!config.isSnowEnabled());
        if (!config.isSnowEnabled()) {
            FLAKES.clear();
            SNOWY_TOPS.clear();
        }
    }

    public static void adjustIntensity(int amount) {
        KalanConfig config = KalanConfig.get();
        config.setSnowIntensity(config.getSnowIntensity() + amount);
    }

    private static final class Snowflake {
        private double x, y, z;
        private final double driftX, fallSpeed, driftZ;
        private final float size;

        private Snowflake(double x, double y, double z, double driftX,
                          double fallSpeed, double driftZ, float size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.driftX = driftX;
            this.fallSpeed = fallSpeed;
            this.driftZ = driftZ;
            this.size = size;
        }

        private Vec3d position() {
            return new Vec3d(x, y, z);
        }
    }
}
