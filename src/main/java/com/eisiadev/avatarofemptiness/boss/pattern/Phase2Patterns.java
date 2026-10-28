package com.eisiadev.avatarofemptiness.boss.pattern;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.SphereVariant;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;

public final class Phase2Patterns {
    public static class MetaphysicalPessimist extends Pattern {
        public MetaphysicalPessimist(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 125; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (at(30)) riftOpen();
            if (at(60)) {
                vanilla("block.respawn_anchor.deplete", 1.0f);
                Hazard h = hazards().spawn(HazardType.BIG_PORTAL, bx(), Arena.FLOOR_Y + 0.03, bz());
                h.size = 6.0f;
                h.life = 50;
            }
            if (at(80)) {
                boss.spawnViscera();
                burst(16f, 0.8f);
                sound(ModSounds.BLOOD_EX, 1.0f);
            }
        }
    }

    public static class MetaphysicalPessimistII extends Pattern {
        public MetaphysicalPessimistII(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 20 + 80 + 60; }

        @Override protected void tick() {
            if (at(0)) riftCharge();
            if (every(20, 20, 5)) {
                sound(ModSounds.RIFT_EJECT, 1.0f);
                vanilla("block.respawn_anchor.deplete", 1.0f);
                for (ServerPlayer p : players()) ninePattern(boss.predict(p, 20));
            }
        }
    }

    public static class RealityCrashI extends Pattern {
        public RealityCrashI(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 135; }

        @Override protected void tick() {
            if (at(0)) sound(ModSounds.SUBZERO_CHARGE, 1.0f);
            if (at(30)) {
                sound(ModSounds.SUBZERO_IMPACT, 1.0f);
                boss.setVariant(SphereVariant.TINT_WHITE, 0xFFFFFFFF, 20, 1.5f);
            }
            if (at(50)) spinRandom(80, BossTuning.SPIN_RATE);
            if (every(50, 5, 16)) inboundComets();
            if (at(130)) restoreDimension();
        }

        @Override public void onEnd() {
            restoreDimension();
            boss.startSpin(0, 0);
        }
    }

    public static class RealityCrashII extends Pattern {
        public RealityCrashII(AvatarEntity boss) { super(boss); }

        private static final int CHARGE_START = 90;
        private static final int CHARGE = 75;

        @Override public int duration() { return CHARGE_START + CHARGE + 10; }

        @Override protected void tick() {
            if (at(0)) sound(ModSounds.SUBZERO_CHARGE, 1.0f);
            if (at(30)) {
                sound(ModSounds.SUBZERO_IMPACT, 1.0f);
                boss.setVariant(SphereVariant.TINT_WHITE, 0xFFFFFFFF, 20, 1.5f);
            }
            if (at(50)) spinRandom(40, BossTuning.SPIN_RATE * 1.5f);
            if (every(50, 2, 20)) inboundComets();
            if (at(CHARGE_START)) {
                sound(ModSounds.ABSOLUTE_ZERO_CHARGE, 1.0f);
                dangerZone(32f, CHARGE, 0.5f);
            }
            if (between(CHARGE_START, CHARGE_START + CHARGE - 3)) pullPlayers(bx(), bz(), BossTuning.PULL_STRONG);
            if (at(CHARGE_START + CHARGE)) {
                sound(ModSounds.ABSOLUTE_ZERO_IMPACT, 1.0f);
                boss.screenFx(0x88FFFFFF, 10, 3.0f);
                boss.screenShatter();
                burst(32f, 0f);
                restoreDimension();
            }
        }

        @Override public void onEnd() {
            restoreDimension();
            boss.startSpin(0, 0);
        }
    }

    public static class VisceralRemnant extends Pattern {
        public VisceralRemnant(AvatarEntity boss) { super(boss); }

        @Override public int duration() { return 40 + 120 + 25; }

        @Override protected void tick() {
            if (at(0)) sound(ModSounds.VISCERAL_ACTIVE, 1.0f);
            if (at(40)) {
                sound(ModSounds.VISCERAL_START, 1.0f);
                sound(ModSounds.VISCERAL_LOOP, 1.0f);
                boss.setVariant(SphereVariant.TINT_RED, 0xFFAA0000, 20, 1.5f);
            }
            if (every(40, 2, 60)) starsRandom(4, BossTuning.ATTACK_RADIUS, 20, BossTuning.STAR_DAMAGE_VISCERAL);
            if (every(40, 20, 5)) {
                vanilla("block.respawn_anchor.deplete", 1.0f);
                for (ServerPlayer p : players()) ninePattern(boss.predict(p, 20));
            }
            if (at(160)) {
                restoreDimension();
            }
        }

        @Override public void onEnd() {
            restoreDimension();
        }
    }

    private Phase2Patterns() {}
}
