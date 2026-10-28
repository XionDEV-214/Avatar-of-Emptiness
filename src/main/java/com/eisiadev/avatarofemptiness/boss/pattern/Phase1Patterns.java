package com.eisiadev.avatarofemptiness.boss.pattern;

import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class Phase1Patterns {
    public static class StarFeastI extends Pattern {
        public StarFeastI(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 130; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (at(30)) riftOpen();
            if (at(60)) {
                sound(ModSounds.BLOOD_STAR_SUMMON, 1.0f);
                sound(ModSounds.BLOOD_STAR_SUMMON, 0.8f);
                starsRandom(40, BossTuning.ATTACK_RADIUS, 60, BossTuning.STAR_DAMAGE);
            }
            if (at(90)) sound(ModSounds.BLOOD_STAR_SEVER, 0.8f);
            if (at(94)) sound(ModSounds.BLOOD_STAR_SEVER, 0.85f);
            if (at(98)) sound(ModSounds.BLOOD_STAR_SEVER, 0.9f);
            if (at(102)) sound(ModSounds.BLOOD_STAR_SEVER, 1.0f);
        }
    }

    public static class ThornFeast extends Pattern {
        public ThornFeast(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 115; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (at(30)) riftOpen();
            if (at(60)) {
                vanilla("block.respawn_anchor.deplete", 1.0f);
                for (int i = 0; i < 64; i++) {
                    Vec3 p = com.eisiadev.avatarofemptiness.arena.Arena.randomInDisk(random(), bx(), bz(), BossTuning.ATTACK_RADIUS, 0);
                    thornPortal(p.x, p.z);
                }
            }
        }
    }

    public static class AmbergrisI extends Pattern {
        public AmbergrisI(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 60 * 2 + 20 + 70; }

        @Override protected void tick() {
            if (every(0, 60, 3)) {
                sound(ModSounds.RIFT_ABSORB, 1.0f);
                riftChargeLight();
            }
            if (every(20, 60, 3)) {
                burst(12f, 0.4f);
                sound(ModSounds.RIFT_EJECT, 1.0f);
                vanilla("block.respawn_anchor.deplete", 1.0f);
                for (ServerPlayer p : players()) {
                    double base = angleTo(boss.predict(p, 15));
                    for (int d = -40; d <= 40; d += 20) {
                        thornLine(base + Math.toRadians(d), BossTuning.THORN_LINE_SPACING, BossTuning.THORN_LINE_RANGE);
                    }
                }
            }
        }

        private void riftChargeLight() {
            var h = hazards().spawn(com.eisiadev.avatarofemptiness.boss.hazard.HazardType.RIFT_PULSE,
                    bx(), com.eisiadev.avatarofemptiness.arena.Arena.FLOOR_Y + 1.2, bz());
            h.size = 20f;
            h.life = 20;
        }
    }

    public static class AmbergrisII extends Pattern {
        public AmbergrisII(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 20 + 110 + 60; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (at(20)) sound(ModSounds.RIFT_EJECT, 1.0f);
            if (every(20, 10, 12)) {
                int shot = index(20, 10) + 1;
                burst(8f, 0.2f);
                vanilla("block.respawn_anchor.deplete", 1.0f);
                for (ServerPlayer p : players()) {
                    double base = angleTo(p.position());
                    for (int d = -15; d <= 15; d += 15) {
                        thornLine(base + Math.toRadians(d), BossTuning.THORN_LINE_SPACING, BossTuning.THORN_LINE_RANGE);
                    }
                }
                if (shot % 4 == 0) shockwave();
            }
        }
    }

    public static class StarFeastII extends Pattern {
        public StarFeastII(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 60 + 80 + 30; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (at(30)) riftOpen();
            if (every(60, 5, 16)) {
                sound(ModSounds.BLOOD_STAR_SUMMON, 1.0f);
                starsRandom(6, BossTuning.ATTACK_RADIUS, 20, BossTuning.STAR_DAMAGE);
            }
            if (every(70, 20, 4)) sound(ModSounds.BLOOD_STAR_SEVER, 0.8f);
        }
    }

    private Phase1Patterns() {}
}
