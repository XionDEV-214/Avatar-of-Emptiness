package com.eisiadev.avatarofemptiness.arena;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Arena {
    public static final ResourceKey<Level> LEVEL =
            ResourceKey.create(Registries.DIMENSION, AvatarOfEmptinessMod.id("arena"));

    public static final double CX = 0.5;
    public static final double CZ = 0.5;
    public static final double FLOOR_Y = 1.0;
    public static final double RADIUS = 64.0;

    public static final Vec3 CENTER = new Vec3(CX, FLOOR_Y, CZ);
    public static final Vec3 PLAYER_START = new Vec3(CX, FLOOR_Y, CZ + 24.0);
    public static final float PLAYER_START_YAW = 180.0f;

    public static boolean isArena(Level level) {
        return level != null && LEVEL.equals(level.dimension());
    }

    public static Vec3 randomInDisk(RandomSource random, double cx, double cz, double radius, double y) {
        double r = radius * Math.sqrt(random.nextDouble());
        double a = random.nextDouble() * Math.PI * 2.0;
        return new Vec3(cx + Math.cos(a) * r, y, cz + Math.sin(a) * r);
    }

    public static boolean inside(double x, double y, double z, double margin) {
        double dx = x - CX, dy = y - FLOOR_Y, dz = z - CZ;
        double r = RADIUS - margin;
        return dx * dx + dy * dy + dz * dz <= r * r;
    }

    public static Vec3 clampInside(Vec3 p, double margin) {
        double dx = p.x - CX, dy = p.y - FLOOR_Y, dz = p.z - CZ;
        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double r = RADIUS - margin;
        if (d <= r || d < 1.0e-6) return p;
        double s = r / d;
        return new Vec3(CX + dx * s, FLOOR_Y + dy * s, CZ + dz * s);
    }

    public static double horizontalDistance(double x, double z, double cx, double cz) {
        return Mth.sqrt((float) ((x - cx) * (x - cx) + (z - cz) * (z - cz)));
    }

    private Arena() {}
}
