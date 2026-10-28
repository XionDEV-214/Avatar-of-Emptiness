package com.eisiadev.avatarofemptiness.boss.pattern;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.SphereVariant;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardManager;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public abstract class Pattern {
    protected final AvatarEntity boss;
    protected int t;

    protected Pattern(AvatarEntity boss) {
        this.boss = boss;
    }

    public abstract int duration();

    protected abstract void tick();

    public final boolean step() {
        tick();
        t++;
        return t >= duration();
    }

    public void onEnd() {}

    protected boolean at(int tick) {
        return t == tick;
    }

    protected boolean every(int start, int interval, int count) {
        int d = t - start;
        return d >= 0 && d % interval == 0 && d / interval < count;
    }

    protected int index(int start, int interval) {
        return (t - start) / interval;
    }

    protected boolean between(int from, int toExclusive) {
        return t >= from && t < toExclusive;
    }

    protected ServerLevel level() {
        return (ServerLevel) boss.level();
    }

    protected RandomSource random() {
        return boss.getRandom();
    }

    protected List<ServerPlayer> players() {
        return boss.arenaPlayers();
    }

    protected HazardManager hazards() {
        return boss.hazards();
    }

    protected double bx() { return boss.getX(); }
    protected double bz() { return boss.getZ(); }

    protected void sound(Supplier<SoundEvent> sound, float pitch) {
        sound(sound.get(), pitch);
    }

    protected void sound(SoundEvent sound, float pitch) {
        level().playSound(null, boss.getX(), boss.getY() + 3.0, boss.getZ(), sound, SoundSource.HOSTILE, 5.0f, pitch);
    }

    protected void vanilla(String path, float pitch) {
        sound(ModSounds.vanilla(path), pitch);
    }

    private float starCarry;

    protected int starCount(int base) {
        float exact = base * BossTuning.STAR_COUNT_SCALE + starCarry;
        int n = (int) exact;
        starCarry = exact - n;
        return n;
    }

    protected void later(int delay, Runnable r) {
        boss.schedule(delay, r);
    }

    protected void riftCharge() {
        sound(ModSounds.RIFT_ABSORB, 1.0f);
        for (int i = 0; i < 3; i++) {
            later(i * 5, () -> {
                Hazard h = hazards().spawn(HazardType.RIFT_PULSE, bx(), Arena.FLOOR_Y + 1.2, bz());
                h.size = 20f;
                h.life = 20;
            });
        }
    }

    protected void riftOpen() {
        sound(ModSounds.RIFT_OPEN, 1.0f);
        burst(24f, 0.6f);
    }

    protected void burst(float radius, float shake) {
        Hazard h = hazards().spawn(HazardType.RIFT_BURST, bx(), Arena.FLOOR_Y + 0.5, bz());
        h.size = radius;
        h.life = 16;
        if (shake > 0) boss.screenFx(0, 0, shake);
    }

    protected void transmission() {
        sound(ModSounds.UA_VORTEX, 1.0f);
        Hazard h = hazards().spawn(HazardType.VORTEX, bx(), Arena.FLOOR_Y + 1.2, bz());
        h.size = 22f;
        h.life = BossTuning.TRANSMISSION_TICKS;
    }

    protected void shiftDimension(SphereVariant variant, int flashRgb) {
        boss.setVariant(variant, 0xFF000000 | flashRgb, 20, 1.2f);
        sound(ModSounds.RIFT_EJECT, 1.0f);
    }

    protected void shatterIntoWhite(SphereVariant variant) {
        sound(ModSounds.ABSOLUTE_ZERO_IMPACT, 1.0f);
        sound(ModSounds.SUBZERO_IMPACT, 0.7f);
        boss.screenFx(0, 0, 3.0f);
        boss.screenShatter();
        later(1, () -> boss.setVariant(variant, 0, 0, 0f));
    }

    protected void whiteoutRestore() {
        boss.whiteout(BossTuning.WHITEOUT_HOLD, BossTuning.WHITEOUT_FADE);
        restoreDimension();
        boss.screenShatter(true);
        sound(ModSounds.SUBZERO_IMPACT, BossTuning.SHATTER_SOUND_PITCH);
    }

    protected void restoreDimension() {
        boss.setVariant(boss.baseVariant(), 0, 0, 0f);
    }

    protected void disgustingStar(double x, double z, int fuse, float damage) {
        Hazard h = hazards().spawn(HazardType.DISGUSTING_STAR, x, Arena.FLOOR_Y + BossTuning.STAR_HEIGHT, z);
        h.size = BossTuning.STAR_RADIUS;
        h.trigger = fuse;
        h.life = fuse + 8;
        h.damage = damage;
        h.setTarget(bx(), boss.getY() + 6.0, bz());
    }

    protected void starsRandom(int baseCount, double radius, int fuse, float damage) {
        int count = starCount(baseCount);
        for (int i = 0; i < count; i++) {
            Vec3 p = Arena.randomInDisk(random(), bx(), bz(), radius, 0);
            disgustingStar(p.x, p.z, fuse, damage);
        }
    }

    protected void thornPortal(double x, double z) {
        if (!Arena.inside(x, Arena.FLOOR_Y, z, 1.5)) return;
        Hazard h = hazards().spawn(HazardType.THORN_PORTAL, x, Arena.FLOOR_Y + 0.02, z);
        h.size = BossTuning.THORN_PORTAL_RADIUS;
        h.trigger = BossTuning.THORN_RISE_DELAY;
        h.life = BossTuning.THORN_LIFE;
        h.damage = BossTuning.THORN_DAMAGE;
        h.a = random().nextFloat() * (float) (Math.PI * 2.0);
    }

    protected void thornLine(double angle, double spacing, double range) {
        double dx = Math.cos(angle), dz = Math.sin(angle);
        int n = (int) (range / spacing);
        for (int i = 1; i <= n; i++) {
            final double px = bx() + dx * spacing * i;
            final double pz = bz() + dz * spacing * i;
            later(i / 2, () -> thornPortal(px, pz));
        }
    }

    protected void ninePattern(Vec3 c) {
        AvatarEntity.ninePattern(boss, c.x, c.z);
    }

    protected void cometVolley() {
        sound(ModSounds.SUBZERO_SHOOT, 1.0f);
        float yaw = boss.getYRot();
        int n = BossTuning.COMET_DIRECTIONS;
        for (int i = 0; i < n; i++) {
            double ang = Math.toRadians(yaw + i * (360.0 / n));
            double dx = -Math.sin(ang), dz = Math.cos(ang);
            Hazard h = hazards().spawn(HazardType.COMET, bx() + dx * 2.0, Arena.FLOOR_Y + 1.0, bz() + dz * 2.0);
            h.setVel(dx * BossTuning.COMET_SPEED, 0, dz * BossTuning.COMET_SPEED);
            h.life = (int) (100 / BossTuning.COMET_SPEED) + 2;
            h.size = 0.6f;
            h.damage = BossTuning.COMET_DAMAGE;
            h.a = (float) ang;
        }
    }

    protected void inboundComets() {
        sound(ModSounds.SUBZERO_SHOOT, 1.0f);
        double r = Arena.RADIUS - 1.0;
        for (int i = 0; i < BossTuning.INBOUND_COMETS; i++) {
            double ang = random().nextDouble() * Math.PI * 2.0;
            double sx = Arena.CX + Math.cos(ang) * r;
            double sz = Arena.CZ + Math.sin(ang) * r;
            double y = Arena.FLOOR_Y + 0.8 + random().nextDouble() * 2.5;
            double dx = bx() - sx, dz = bz() - sz;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0e-4) continue;
            dx /= len;
            dz /= len;
            Hazard h = hazards().spawn(HazardType.COMET, sx, y, sz);
            h.setVel(dx * BossTuning.COMET_SPEED, 0, dz * BossTuning.COMET_SPEED);
            h.setTarget(bx(), y, bz());
            h.b = 1f;
            h.life = (int) (len / BossTuning.COMET_SPEED) + 4;
            h.size = 0.6f;
            h.damage = BossTuning.COMET_DAMAGE;
            h.a = (float) Math.atan2(-dx, dz);
        }
    }

    protected void shockwave() {
        Hazard h = hazards().spawn(HazardType.SHOCKWAVE, bx(), Arena.FLOOR_Y, bz());
        h.a = BossTuning.SHOCKWAVE_SPEED;
        h.b = (float) Arena.RADIUS;
        h.life = (int) (Arena.RADIUS / BossTuning.SHOCKWAVE_SPEED);
        h.damage = BossTuning.SHOCKWAVE_DAMAGE;
        sound(ModSounds.RIFT_EJECT, 1.3f);
    }

    protected void dangerZone(float radius, int delay, float fraction) {
        Hazard h = hazards().spawn(HazardType.DANGER_ZONE, bx(), Arena.FLOOR_Y, bz());
        h.size = radius;
        h.trigger = delay;
        h.life = delay + 14;
        h.a = fraction;
    }

    protected void pullPlayers(double cx, double cz, double strength) {
        for (ServerPlayer p : players()) {
            Vec3 d = new Vec3(cx - p.getX(), 0, cz - p.getZ());
            double len = d.length();
            if (len < 0.5) continue;
            Vec3 v = p.getDeltaMovement().add(d.scale(strength / len));
            p.setDeltaMovement(v);
            p.hurtMarked = true;
        }
    }

    protected void spinRandom(int ticks, float degPerTick) {
        boss.startSpin(ticks, random().nextBoolean() ? degPerTick : -degPerTick);
    }

    protected double angleTo(Vec3 target) {
        return Math.atan2(target.z - bz(), target.x - bx());
    }
}
