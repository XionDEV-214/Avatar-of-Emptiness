package com.eisiadev.avatarofemptiness.boss;

import com.eisiadev.avatarofemptiness.AoeConfig;
import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.ArenaFight;
import com.eisiadev.avatarofemptiness.arena.SphereVariant;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardManager;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.boss.pattern.Pattern;
import com.eisiadev.avatarofemptiness.boss.pattern.Patterns;
import com.eisiadev.avatarofemptiness.net.ArenaFxPacket;
import com.eisiadev.avatarofemptiness.net.HazardSyncPacket;
import com.eisiadev.avatarofemptiness.net.ModNetwork;
import com.eisiadev.avatarofemptiness.net.MusicPacket;
import com.eisiadev.avatarofemptiness.net.ScreenShatterPacket;
import com.eisiadev.avatarofemptiness.registry.ModEntities;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AvatarEntity extends Monster {
    public static final int VISUAL_RIFT = 0;
    public static final int VISUAL_MODEL = 1;
    public static final int VISUAL_HIDDEN = 2;
    public static final int VISUAL_STATIC = 3;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VISUAL =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRANSITION =
            SynchedEntityData.defineId(AvatarEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            BossPhase.RIFT.displayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6)
            .setDarkenScreen(true);

    private final HazardManager hazards = new HazardManager(this);
    private final List<Scheduled> scheduled = new ArrayList<>();
    private final Map<UUID, Integer> contactHits = new HashMap<>();
    private final Map<UUID, Vec3> lastPositions = new HashMap<>();
    private final Map<UUID, Vec3> velocities = new HashMap<>();
    private final Map<UUID, Integer> airTicks = new HashMap<>();
    private List<ServerPlayer> players = List.of();

    private BossPhase phase = BossPhase.RIFT;
    private Vec3 anchor;
    private boolean initialized;

    private Pattern current;
    private long nextPatternTick;
    private long globalCooldownUntil;
    private final Map<String, Long> patternCooldowns = new HashMap<>();

    private int transitionTicks;
    private int spinTicks;
    private float spinRate;
    private SphereVariant variant = SphereVariant.STANDARD;
    private int paradiseTicks = -1;
    private int victoryTicks = -1;
    private int emptyTicks;
    private int hurtSoundCooldown;

    private record Scheduled(int due, Runnable action) {}

    public AvatarEntity(EntityType<? extends AvatarEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BossPhase.RIFT.maxHealth)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 256.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_VISUAL, VISUAL_RIFT);
        this.entityData.define(DATA_TRANSITION, 0);
    }

    public int getPhaseNumber() {
        return this.entityData.get(DATA_PHASE);
    }

    public int getVisual() {
        return this.entityData.get(DATA_VISUAL);
    }

    public int getTransitionTicks() {
        return this.entityData.get(DATA_TRANSITION);
    }

    public HazardManager hazards() {
        return hazards;
    }

    public List<ServerPlayer> arenaPlayers() {
        return players;
    }

    public BossPhase phase() {
        return phase;
    }

    public double health() {
        return getHealth();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return false;
        if (source.is(DamageTypes.GENERIC_KILL)) {
            endFight(false);
            return false;
        }
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Player player) || player.isSpectator()) return false;
        if (transitionTicks > 0 || victoryTicks >= 0 || getVisual() == VISUAL_HIDDEN) return false;
        if (amount <= 0) return false;

        BossPhase before = phase;
        boolean result = super.hurt(source, amount);
        if (result) {
            playHurt();
            if (phase == before && victoryTicks < 0) tryStartPattern();
        }
        updateBar();
        return result;
    }

    @Override
    public void die(DamageSource source) {
        if (this.level().isClientSide || victoryTicks >= 0) return;
        advancePhase();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    private void updateBar() {
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0f, 1f));
    }

    private void applyMaxHealth(BossPhase p) {
        AttributeInstance attr = getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(p.maxHealth);
        setHealth(getMaxHealth());
    }

    private void playHurt() {
        if (hurtSoundCooldown > 0) return;
        if (phase == BossPhase.RIFT) {
            hurtSoundCooldown = 1;
            level().playSound(null, getX(), getY() + 6, getZ(), ModSounds.HURT_RIFT.get(), SoundSource.HOSTILE, 5f, 1.0f);
            level().playSound(null, getX(), getY() + 6, getZ(), ModSounds.HURT_RIFT.get(), SoundSource.HOSTILE, 5f, 1.2f);
        } else {
            hurtSoundCooldown = 2;
            level().playSound(null, getX(), getY() + 6, getZ(), ModSounds.HURT.get(), SoundSource.HOSTILE, 5f, 1.0f);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
        ModNetwork.sendTo(player, new HazardSyncPacket(true, new ArrayList<>(hazards.all())));
        ModNetwork.sendTo(player, ArenaFxPacket.variantOnly(variant.ordinal()));
        if (victoryTicks < 0) {
            ModNetwork.sendTo(player, new MusicPacket(phase.music.getId(), phase.loopMusic));
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
        ModNetwork.sendTo(player, MusicPacket.stop());
        ModNetwork.sendTo(player, new HazardSyncPacket(true, List.of()));
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        bossEvent.removeAllPlayers();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            serverTick(server);
        }
    }

    private void serverTick(ServerLevel level) {
        if (!initialized) {
            initialized = true;
            anchor = position();
            setPhase(BossPhase.RIFT);
            applyMaxHealth(phase);
            transitionTicks = BossTuning.INTRO_TICKS;
            this.entityData.set(DATA_TRANSITION, transitionTicks);
            level.playSound(null, getX(), getY() + 6, getZ(), ModSounds.RIFT_OPEN.get(), SoundSource.HOSTILE, 6f, 0.8f);
        }

        if (anchor != null) {
            setPos(anchor.x, anchor.y, anchor.z);
        }
        setDeltaMovement(Vec3.ZERO);
        if (hurtSoundCooldown > 0) hurtSoundCooldown--;

        refreshPlayers(level);

        if (victoryTicks >= 0) {
            victoryTick(level);
            hazards.tick(level, players);
            return;
        }

        if (players.isEmpty()) {
            if (++emptyTicks > 100) endFight(false);
            return;
        }
        emptyTicks = 0;

        trackVelocities();
        antiFlight(level);

        if (spinTicks > 0) {
            spinTicks--;
            float yaw = Mth.wrapDegrees(getYRot() + spinRate);
            setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }

        runScheduled();

        if (transitionTicks > 0) {
            transitionTicks--;
            this.entityData.set(DATA_TRANSITION, transitionTicks);
            if (transitionTicks == 0) onTransitionEnd(level);
        } else if (current != null) {
            if (current.step()) {
                current.onEnd();
                current = null;
            }
        }

        if (phase == BossPhase.PARADISE && paradiseTicks >= 0) paradiseTick();

        contactDamage();
        hazards.tick(level, players);
        updateBar();
    }

    private void refreshPlayers(ServerLevel level) {
        players = level.getPlayers(p -> p.isAlive() && !p.isSpectator()
                && Arena.inside(p.getX(), p.getY(), p.getZ(), -16.0));
    }

    private void trackVelocities() {
        for (ServerPlayer p : players) {
            Vec3 pos = p.position();
            Vec3 last = lastPositions.put(p.getUUID(), pos);
            if (last != null) {
                Vec3 v = pos.subtract(last);
                Vec3 old = velocities.getOrDefault(p.getUUID(), Vec3.ZERO);
                velocities.put(p.getUUID(), old.scale(0.6).add(v.scale(0.4)));
            }
        }
    }

    private void antiFlight(ServerLevel level) {
        for (ServerPlayer p : players) {
            UUID id = p.getUUID();
            if (p.onGround() || p.isInWater() || p.isPassenger() || p.getAbilities().flying) {
                airTicks.remove(id);
                continue;
            }
            int ticks = airTicks.merge(id, 1, Integer::sum);
            if (ticks < BossTuning.AIR_SLAM_TICKS) continue;
            airTicks.remove(id);
            p.stopFallFlying();
            p.setDeltaMovement(0, -BossTuning.AIR_SLAM_SPEED, 0);
            p.hurtMarked = true;
            level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.SUBZERO_IMPACT.get(), SoundSource.HOSTILE, 2.0f, 1.4f);
            ModNetwork.sendTo(p, new ArenaFxPacket(-1, 0x66FFFFFF, 6, 2.0f));
        }
    }

    public Vec3 predict(ServerPlayer player, int ticksAhead) {
        Vec3 v = velocities.getOrDefault(player.getUUID(), Vec3.ZERO);
        Vec3 p = player.position().add(v.x * ticksAhead, 0, v.z * ticksAhead);
        return Arena.clampInside(new Vec3(p.x, Arena.FLOOR_Y, p.z), 2.0);
    }

    private void contactDamage() {
        if (getVisual() == VISUAL_HIDDEN) return;
        AABB box = getBoundingBox();
        int interval = AoeConfig.CONTACT_DAMAGE_INTERVAL.get();
        for (ServerPlayer p : players) {
            if (!p.getBoundingBox().intersects(box)) continue;
            int last = contactHits.getOrDefault(p.getUUID(), -100000);
            if (tickCount - last >= interval) {
                contactHits.put(p.getUUID(), tickCount);
                BossDamage.hit(p, this, phase.contactDamage);
            }
        }
    }

    public void schedule(int delay, Runnable action) {
        if (delay <= 0) {
            action.run();
        } else {
            scheduled.add(new Scheduled(tickCount + delay, action));
        }
    }

    private void runScheduled() {
        if (scheduled.isEmpty()) return;
        List<Scheduled> due = new ArrayList<>();
        scheduled.removeIf(s -> {
            if (s.due() <= tickCount) {
                due.add(s);
                return true;
            }
            return false;
        });
        for (Scheduled s : due) s.action().run();
    }

    public void startSpin(int ticks, float degPerTick) {
        spinTicks = ticks;
        spinRate = degPerTick;
    }

    private void tryStartPattern() {
        if (current != null || transitionTicks > 0 || victoryTicks >= 0 || phase == BossPhase.PARADISE) return;
        long now = level().getGameTime();
        List<Patterns.Entry> pool = new ArrayList<>();
        for (Patterns.Entry e : Patterns.forPhase(phase)) {
            if (!e.available().test(this)) continue;
            if (phase == BossPhase.WRATH && patternCooldowns.getOrDefault(e.id(), 0L) > now) continue;
            pool.add(e);
        }
        if (pool.isEmpty()) return;
        if (phase == BossPhase.WRATH) {
            if (now < globalCooldownUntil) return;
        } else if (now < nextPatternTick) {
            return;
        }
        Patterns.Entry pick = pool.get(getRandom().nextInt(pool.size()));
        startPattern(pick);
    }

    public void startPattern(Patterns.Entry entry) {
        cancelPattern();
        long now = level().getGameTime();
        nextPatternTick = now + BossTuning.PHASE12_PATTERN_COOLDOWN;
        if (entry.ownCooldown() > 0) patternCooldowns.put(entry.id(), now + entry.ownCooldown());
        if (entry.globalCooldown() > 0) globalCooldownUntil = now + entry.globalCooldown();
        current = entry.factory().apply(this);
        AvatarOfEmptinessMod.LOGGER.debug("Avatar pattern start: {}", entry.id());
    }

    public void cancelPattern() {
        if (current != null) {
            current.onEnd();
            current = null;
        }
        scheduled.clear();
        spinTicks = 0;
    }

    private void setPhase(BossPhase p) {
        phase = p;
        this.entityData.set(DATA_PHASE, p.number);
        this.entityData.set(DATA_VISUAL, baseVisual());
        bossEvent.setName(p.displayName());
        setCustomName(p.displayName());
        bossEvent.setProgress(1.0f);
    }

    public int baseVisual() {
        return switch (phase) {
            case RIFT -> VISUAL_RIFT;
            case AVATAR, WRATH -> VISUAL_MODEL;
            case PARADISE -> VISUAL_STATIC;
        };
    }

    public SphereVariant baseVariant() {
        return phase == BossPhase.PARADISE ? SphereVariant.PARADISE : SphereVariant.STANDARD;
    }

    public void setVisual(int visual) {
        this.entityData.set(DATA_VISUAL, visual);
    }

    public void forcePhase(BossPhase target) {
        if (!(level() instanceof ServerLevel)) return;
        cancelPattern();
        while (phase != target && phase.next() != null) {
            advancePhase();
            transitionTicks = Math.min(transitionTicks, 5);
        }
    }

    private void advancePhase() {
        ServerLevel level = (ServerLevel) level();
        cancelPattern();
        hazards.clearAll(level);
        discardViscera();
        spinTicks = 0;

        BossPhase next = phase.next();
        if (next == null) {
            victory(level);
            return;
        }
        setPhase(next);
        applyMaxHealth(next);
        nextPatternTick = 0;
        globalCooldownUntil = 0;
        patternCooldowns.clear();

        level.playSound(null, getX(), getY() + 6, getZ(), ModSounds.RIFT_EJECT.get(), SoundSource.HOSTILE, 6f, 0.8f);
        level.playSound(null, getX(), getY() + 6, getZ(), ModSounds.RIFT_OPEN.get(), SoundSource.HOSTILE, 6f, 1.0f);

        if (next == BossPhase.PARADISE) {
            setVariant(SphereVariant.PARADISE, 0xFFFFFFFF, 40, 3.0f);
            transitionTicks = 20;
            paradiseTicks = -1;
        } else {
            setVariant(SphereVariant.STANDARD, 0xFFFFFFFF, 30, 2.5f);
            transitionTicks = BossTuning.PHASE_TRANSITION_TICKS;
        }
        this.entityData.set(DATA_TRANSITION, transitionTicks);
        ModNetwork.sendToLevel(level, new MusicPacket(next.music.getId(), next.loopMusic));
    }

    private void onTransitionEnd(ServerLevel level) {
        if (phase == BossPhase.PARADISE && paradiseTicks < 0) {
            paradiseTicks = 0;
            double lowest = Arena.FLOOR_Y;
            for (ServerPlayer p : players) lowest = Math.min(lowest, p.getY());
            Hazard floor = hazards.spawn(HazardType.PARADISE_FLOOR, getX(), lowest - BossTuning.PARADISE_FLOOR_START_DEPTH, getZ());
            floor.setVel(0, BossTuning.PARADISE_FLOOR_SPEED, 0);
            floor.size = (float) Arena.RADIUS;
            floor.life = BossTuning.PARADISE_TIME_LIMIT + 200;
        }
    }

    private void paradiseTick() {
        paradiseTicks++;
        if (paradiseTicks % 20 == 0) {
            for (ServerPlayer p : players) BossDamage.hit(p, this, BossTuning.PARADISE_TICK_DAMAGE);
        }
        if (paradiseTicks >= BossTuning.PARADISE_TIME_LIMIT) {
            for (ServerPlayer p : new ArrayList<>(players)) BossDamage.kill(p, this);
        }
    }

    private void victory(ServerLevel level) {
        victoryTicks = 0;
        setHealth(1f);
        setVisual(VISUAL_HIDDEN);
        bossEvent.setProgress(0f);
        ModNetwork.sendToLevel(level, MusicPacket.stop());
        screenFx(0xFFFFFFFF, 60, 2.0f);
        level.playSound(null, getX(), getY() + 6, getZ(), ModSounds.ABSOLUTE_ZERO_IMPACT.get(), SoundSource.HOSTILE, 6f, 0.7f);
        ArenaFight.onVictory(level, this);
    }

    private void victoryTick(ServerLevel level) {
        victoryTicks++;
        if (victoryTicks == 80) {
            for (ServerPlayer p : new ArrayList<>(level.players())) ArenaFight.sendHome(p);
            discard();
        }
    }

    public void endFight(boolean sendPlayersHome) {
        if (level() instanceof ServerLevel level) {
            hazards.clearAll(level);
            discardViscera();
            ModNetwork.sendToLevel(level, MusicPacket.stop());
            ModNetwork.sendToLevel(level, ArenaFxPacket.variantOnly(SphereVariant.STANDARD.ordinal()));
            if (sendPlayersHome) {
                for (ServerPlayer p : new ArrayList<>(level.players())) ArenaFight.sendHome(p);
            }
        }
        discard();
    }

    public void setVariant(SphereVariant v, int flashArgb, int flashTicks, float shake) {
        variant = v;
        if (level() instanceof ServerLevel level) {
            ModNetwork.sendToLevel(level, new ArenaFxPacket(v.ordinal(), flashArgb, flashTicks, shake));
        }
    }

    public void whiteout(int hold, int fade) {
        if (level() instanceof ServerLevel level) {
            ModNetwork.sendToLevel(level, new ArenaFxPacket(-1, 0xFFFFFFFF, fade, 0f, hold));
        }
    }

    public void screenShatter() {
        screenShatter(false);
    }

    public void screenShatter(boolean white) {
        if (level() instanceof ServerLevel level) {
            ModNetwork.sendToLevel(level, new ScreenShatterPacket(white));
        }
    }

    public void screenFx(int flashArgb, int flashTicks, float shake) {
        if (level() instanceof ServerLevel level) {
            ModNetwork.sendToLevel(level, new ArenaFxPacket(-1, flashArgb, flashTicks, shake));
        }
    }

    public void spawnViscera() {
        if (!(level() instanceof ServerLevel level)) return;
        VisceraEntity v = ModEntities.VISCERA.get().create(level);
        if (v == null) return;
        v.moveTo(getX(), Arena.FLOOR_Y + 0.5, getZ(), 0, 0);
        v.setOwner(this);
        v.setDeltaMovement(0, 0.9, 0);
        level.addFreshEntity(v);
    }

    public boolean hasViscera() {
        return !level().getEntitiesOfClass(VisceraEntity.class, getBoundingBox().inflate(Arena.RADIUS * 2)).isEmpty();
    }

    private void discardViscera() {
        for (VisceraEntity v : level().getEntitiesOfClass(VisceraEntity.class, getBoundingBox().inflate(Arena.RADIUS * 2))) {
            v.discard();
        }
    }

    public static void ninePattern(AvatarEntity boss, double cx, double cz) {
        double[][] offsets = {
                {0, 0},
                {15.5, 0}, {-15.5, 0}, {0, 15.5}, {0, -15.5},
                {10, 10}, {10, -10}, {-10, 10}, {-10, -10}
        };
        for (double[] o : offsets) {
            double x = cx + o[0], z = cz + o[1];
            if (!Arena.inside(x, Arena.FLOOR_Y, z, 1.5)) continue;
            Hazard h = boss.hazards().spawn(HazardType.THORN_PORTAL, x, Arena.FLOOR_Y + 0.02, z);
            h.size = BossTuning.THORN_PORTAL_RADIUS;
            h.trigger = BossTuning.THORN_RISE_DELAY;
            h.life = BossTuning.THORN_LIFE;
            h.damage = BossTuning.THORN_DAMAGE;
            h.a = boss.getRandom().nextFloat() * (float) (Math.PI * 2.0);
        }
    }
}
