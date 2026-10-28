package com.eisiadev.avatarofemptiness.boss.pattern;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.SphereVariant;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.world.phys.Vec3;

public final class Phase3Patterns {
    private static final int T = BossTuning.TRANSMISSION_TICKS;

    public static class VisceralFountain extends Pattern {
        public VisceralFountain(AvatarEntity boss) { super(boss); }

        private static final int WARN = T;
        private static final int FIRE = T + 80;
        private static final int FIRE_TIME = 100;
        private double baseAngle;

        @Override public int duration() { return FIRE + FIRE_TIME + 5; }

        @Override protected void tick() {
            if (at(0)) transmission();
            if (at(T)) shiftDimension(SphereVariant.VISCERAL, 0xAA0000);
            if (at(WARN)) {
                baseAngle = Math.toRadians(boss.getYRot());
                for (int k = 0; k < 8; k++) {
                    Hazard h = hazards().spawn(HazardType.DEATHRAY_WARN, bx(), Arena.FLOOR_Y + 1.0, bz());
                    h.a = (float) (baseAngle + k * Math.PI / 4.0);
                    h.size = BossTuning.DEATHRAY_LENGTH;
                    h.life = FIRE - WARN;
                }
            }
            if (at(T + 30)) sound(ModSounds.BLOOD_CRY, 1.0f);
            if (at(FIRE - 20)) sound(ModSounds.VISCERAL_ACTIVE, 1.0f);
            if (at(FIRE)) {
                sound(ModSounds.BLOOD_FOUNTAIN, 1.0f);
                sound(ModSounds.VISCERAL_START, 1.0f);
                sound(ModSounds.VISCERAL_LOOP, 1.0f);
                boss.screenFx(0, 0, 1.5f);
                float spin = random().nextBoolean() ? BossTuning.DEATHRAY_SPIN : -BossTuning.DEATHRAY_SPIN;
                for (int k = 0; k < 8; k++) {
                    Hazard h = hazards().spawn(HazardType.DEATHRAY, bx(), Arena.FLOOR_Y + 1.0, bz());
                    h.a = (float) (baseAngle + k * Math.PI / 4.0);
                    h.b = spin;
                    h.size = BossTuning.DEATHRAY_LENGTH;
                    h.life = FIRE_TIME;
                }
            }
            if (every(FIRE, 2, 50)) starsRandom(4, BossTuning.ATTACK_RADIUS, 20, BossTuning.STAR_DAMAGE_VISCERAL);
            if (at(FIRE + FIRE_TIME)) {
                sound(ModSounds.RIFT_OPEN, 1.0f);
                restoreDimension();
            }
        }

        @Override public void onEnd() { restoreDimension(); }
    }

    public static class RealityShatter extends Pattern {
        public RealityShatter(AvatarEntity boss) { super(boss); }

        private static final int CHARGE_START = T + 130;
        private static final int CHARGE = 75;

        @Override public int duration() { return CHARGE_START + CHARGE + 10; }

        @Override protected void tick() {
            if (at(0)) transmission();
            if (at(T)) {
                shiftDimension(SphereVariant.SHATTER, 0xFFFFFF);
                sound(ModSounds.SUBZERO_CHARGE, 1.0f);
            }
            if (at(T + 30)) {
                sound(ModSounds.SUBZERO_IMPACT, 1.0f);
                boss.screenFx(0xFFFFFFFF, 20, 1.5f);
            }
            if (at(T + 50)) spinRandom(80, BossTuning.SPIN_RATE);
            if (every(T + 50, 2, 40)) inboundComets();
            if (at(CHARGE_START)) {
                sound(ModSounds.ABSOLUTE_ZERO_CHARGE, 1.0f);
                dangerZone(45f, CHARGE, 0.75f);
            }
            if (between(CHARGE_START, CHARGE_START + CHARGE - 3)) pullPlayers(bx(), bz(), BossTuning.PULL_SHATTER);
            if (at(CHARGE_START + CHARGE)) {
                sound(ModSounds.ABSOLUTE_ZERO_IMPACT, 1.0f);
                boss.screenFx(0x88FFFFFF, 10, 3.5f);
                boss.screenShatter();
                burst(45f, 0f);
                restoreDimension();
            }
        }

        @Override public void onEnd() {
            restoreDimension();
            boss.startSpin(0, 0);
        }
    }

    public static class SubzeroOnslaught extends Pattern {
        public SubzeroOnslaught(AvatarEntity boss) { super(boss); }

        private static final int START = T + 30;

        @Override public int duration() { return START + 120 * 4 + 125 + 5; }

        @Override protected void tick() {
            if (at(0)) transmission();
            if (at(T)) shiftDimension(SphereVariant.SUBZERO, 0x66CCFF);
            if (every(START, 120, 5)) {
                sound(ModSounds.SUBZERO_VORTEX_CHARGE, 1.0f);
                later(25, () -> sound(ModSounds.SUBZERO_VORTEX_BURST, 1.0f));
                for (int i = 0; i < 20; i++) {
                    Vec3 p = Arena.randomInDisk(random(), bx(), bz(), 58.0, 0);
                    if (!Arena.inside(p.x, Arena.FLOOR_Y, p.z, BossTuning.TORNADO_RADIUS)) continue;
                    Hazard h = hazards().spawn(HazardType.SUBZERO_PORTAL, p.x, Arena.FLOOR_Y + 0.03, p.z);
                    h.size = BossTuning.TORNADO_RADIUS;
                    h.trigger = 25;
                    h.life = 25 + 100;
                    h.a = random().nextBoolean() ? 1f : -1f;
                }
            }
            if (at(duration() - 3)) restoreDimension();
        }

        @Override public void onEnd() { restoreDimension(); }
    }

    public static class UniversalAnnihilation extends Pattern {
        public UniversalAnnihilation(AvatarEntity boss) { super(boss); }

        private static final int BLAST = T + 50;
        private static final int HOLE_LIFE = 280;
        private static final int STARS_START = BLAST + 60;
        private static final int STAR_BURSTS = 22;
        private double hx, hy, hz;

        @Override public int duration() { return BLAST + HOLE_LIFE + 10; }

        @Override protected void tick() {
            if (at(0)) transmission();
            if (at(T)) shiftDimension(SphereVariant.ANNIHILATION, 0x000000);
            if (at(T + 10)) sound(ModSounds.UA_CHARGE, 1.0f);
            if (at(BLAST)) {
                sound(ModSounds.UA_BLAST, 1.0f);
                sound(ModSounds.UA_LOOP, 1.0f);
                boss.screenFx(0xFFFFFFFF, 30, 2.5f);
                boss.setVisual(AvatarEntity.VISUAL_HIDDEN);
                hx = bx();
                hy = Arena.FLOOR_Y + BossTuning.BLACKHOLE_HEIGHT;
                hz = bz();
                Hazard h = hazards().spawn(HazardType.BLACKHOLE, hx, hy, hz);
                h.size = BossTuning.BLACKHOLE_RADIUS;
                h.life = HOLE_LIFE;
            }
            if (between(BLAST, BLAST + HOLE_LIFE)) pullPlayers(hx, hz, BossTuning.BLACKHOLE_PULL_PLAYER);
            if (every(STARS_START, 10, STAR_BURSTS)) {
                int n = starCount(BossTuning.UA_STARS_PER_BURST);
                for (int i = 0; i < n; i++) spawnStar();
            }
            if (at(BLAST + HOLE_LIFE)) {
                boss.setVisual(boss.baseVisual());
                restoreDimension();
            }
        }

        private void spawnStar() {
            int roll = random().nextInt(13);
            HazardType type;
            float radius, damage;
            if (roll < 10) {
                type = HazardType.STAR_WHITE;
                radius = BossTuning.WHITE_DWARF_RADIUS;
                damage = BossTuning.WHITE_DWARF_DAMAGE;
            } else if (roll < 12) {
                type = HazardType.STAR_BLUE;
                radius = BossTuning.BLUE_STAR_RADIUS;
                damage = BossTuning.BLUE_STAR_DAMAGE;
            } else {
                type = HazardType.STAR_RED;
                radius = BossTuning.RED_GIANT_RADIUS;
                damage = BossTuning.RED_GIANT_DAMAGE;
            }
            double ang = random().nextDouble() * Math.PI * 2.0;
            double r = BossTuning.UA_STAR_SPAWN_RADIUS;
            double x = hx + Math.cos(ang) * r;
            double z = hz + Math.sin(ang) * r;
            double y = Arena.FLOOR_Y + 2.0 + random().nextDouble() * 18.0;
            Hazard h = hazards().spawn(type, x, y, z);
            Vec3 dir = new Vec3(hx - x, hy - y, hz - z).normalize();
            h.setVel(dir.x, dir.y, dir.z);
            h.velMul = 1.05f;
            h.pull = 0.08f;
            h.maxSpeed = 6.0f;
            h.setTarget(hx, hy, hz);
            h.size = radius;
            h.damage = damage;
            h.life = 160;
            h.a = random().nextFloat() * 360f;
        }

        @Override public void onEnd() {
            boss.setVisual(boss.baseVisual());
            restoreDimension();
        }
    }

    private Phase3Patterns() {}
}
