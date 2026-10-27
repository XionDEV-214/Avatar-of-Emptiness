package com.eisiadev.avatarofemptiness.boss;

import com.eisiadev.avatarofemptiness.AoeConfig;
import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisceraEntity extends Monster {
    private static final double SPEED = 0.22;

    @Nullable
    private UUID ownerId;
    @Nullable
    private AvatarEntity ownerCache;
    private final Map<UUID, Integer> contactHits = new HashMap<>();

    public VisceraEntity(EntityType<? extends VisceraEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 128.0);
    }

    public void setOwner(AvatarEntity boss) {
        this.ownerId = boss.getUUID();
        this.ownerCache = boss;
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
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            discard();
            return false;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;

        AvatarEntity boss = owner(level);
        if (boss == null || !boss.isAlive() || boss.phase() != BossPhase.AVATAR) {
            discard();
            return;
        }

        ServerPlayer target = nearest(boss);
        if (target != null) {
            Vec3 d = new Vec3(target.getX() - getX(), 0, target.getZ() - getZ());
            double len = d.length();
            if (len > 0.8) {
                Vec3 m = d.scale(SPEED / len);
                setDeltaMovement(m.x, getDeltaMovement().y, m.z);
                setYRot((float) Math.toDegrees(Math.atan2(-d.x, d.z)));
            }
            if (tickCount % 60 == 0 && tickCount > 0) {
                level.playSound(null, getX(), getY(), getZ(), ModSounds.vanilla("block.respawn_anchor.deplete"),
                        SoundSource.HOSTILE, 5f, 1f);
                AvatarEntity.ninePattern(boss, target.getX(), target.getZ());
                boss.schedule(20, () -> {
                    level.playSound(null, getX(), getY(), getZ(), ModSounds.vanilla("item.trident.thunder"), SoundSource.HOSTILE, 4f, 1f);
                    level.playSound(null, getX(), getY(), getZ(), ModSounds.vanilla("entity.allay.death"), SoundSource.HOSTILE, 4f, 0.6f);
                });
            }
        }

        if (!Arena.inside(getX(), getY(), getZ(), 3.0)) {
            Vec3 c = Arena.clampInside(position(), 3.0);
            setPos(c.x, Math.max(c.y, Arena.FLOOR_Y), c.z);
        }

        int interval = AoeConfig.CONTACT_DAMAGE_INTERVAL.get();
        for (ServerPlayer p : boss.arenaPlayers()) {
            if (!p.getBoundingBox().intersects(getBoundingBox())) continue;
            int last = contactHits.getOrDefault(p.getUUID(), -100000);
            if (tickCount - last >= interval) {
                contactHits.put(p.getUUID(), tickCount);
                BossDamage.hit(p, this, BossPhase.AVATAR.contactDamage);
            }
        }
    }

    @Nullable
    private AvatarEntity owner(ServerLevel level) {
        if (ownerCache != null && !ownerCache.isRemoved()) return ownerCache;
        if (ownerId == null) return null;
        Entity e = level.getEntity(ownerId);
        ownerCache = e instanceof AvatarEntity a ? a : null;
        return ownerCache;
    }

    @Nullable
    private ServerPlayer nearest(AvatarEntity boss) {
        ServerPlayer best = null;
        double bestD = Double.MAX_VALUE;
        for (ServerPlayer p : boss.arenaPlayers()) {
            double d = p.distanceToSqr(this);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }
}
