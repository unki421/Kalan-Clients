package dev.kalan.client;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Random;

public final class KalanHitParticles {
    private static final Random RANDOM = new Random();
    private static boolean initialized;

    private KalanHitParticles() {}

    public static void initialize() {
        if (initialized) return;
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (world.isClient && isEnabled()) spawn(world, target);
            return ActionResult.PASS;
        });
        initialized = true;
    }

    private static void spawn(World world, Entity target) {
        KalanConfig config = KalanConfig.get();
        Box box = target.getBoundingBox();
        int amount = config.getHitParticlesAmount();
        for (int i = 0; i < amount; i++) {
            double x = box.minX + RANDOM.nextDouble() * (box.maxX - box.minX);
            double y = box.minY + RANDOM.nextDouble() * (box.maxY - box.minY);
            double z = box.minZ + RANDOM.nextDouble() * (box.maxZ - box.minZ);
            double vx = (RANDOM.nextDouble() - 0.5) * 0.12;
            double vy = RANDOM.nextDouble() * 0.12;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.12;
            switch (config.getHitParticlesStyle()) {
                case 1 -> world.addParticle(ParticleTypes.ENCHANT, x, y, z, vx, vy, vz);
                case 2 -> world.addParticle(ParticleTypes.END_ROD, x, y, z, vx, vy, vz);
                default -> world.addParticle(ParticleTypes.CRIT, x, y, z, vx, vy, vz);
            }
        }
    }

    public static boolean isEnabled() {
        return KalanConfig.get().isHitParticlesEnabled();
    }

    public static void toggle() {
        KalanConfig config = KalanConfig.get();
        config.setHitParticlesEnabled(!config.isHitParticlesEnabled());
    }

    public static void cycleStyle() {
        KalanConfig config = KalanConfig.get();
        config.setHitParticlesStyle((config.getHitParticlesStyle() + 1) % 3);
    }

    public static void adjustAmount(int delta) {
        KalanConfig config = KalanConfig.get();
        config.setHitParticlesAmount(config.getHitParticlesAmount() + delta);
    }
}
