package dev.kalan.client;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public final class KalanHitParticles {
    private static boolean initialized;

    private KalanHitParticles() {}

    public static void initialize() {
        if (initialized) return;
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (world.isClient && isEnabled() && !target.isRemoved() && target.isAlive()) spawn(world, target);
            return ActionResult.PASS;
        });
        initialized = true;
    }

    private static void spawn(World world, Entity target) {
        KalanConfig config = KalanConfig.get();
        Box box = target.getBoundingBox();
        int amount = config.getHitParticlesAmount();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < amount; i++) {
            double x = box.minX + random.nextDouble() * (box.maxX - box.minX);
            double y = box.minY + 0.12 + random.nextDouble() * Math.max(0.1, box.maxY - box.minY - 0.12);
            double z = box.minZ + random.nextDouble() * (box.maxZ - box.minZ);
            double vx = (random.nextDouble() - 0.5) * 0.22;
            double vy = random.nextDouble() * 0.22;
            double vz = (random.nextDouble() - 0.5) * 0.12;
            switch (config.getHitParticlesStyle()) {
                case 1 -> world.addParticle(ParticleTypes.ENCHANT, x, y, z, vx * 0.7, vy + 0.04, vz * 0.7);
                case 2 -> world.addParticle(ParticleTypes.END_ROD, x, y, z, vx * 0.6, vy + 0.03, vz * 0.6);
                default -> world.addParticle(ParticleTypes.CRIT, x, y, z, vx, vy + 0.03, vz);
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
