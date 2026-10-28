package com.eisiadev.avatarofemptiness.boss.hazard;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossDamage;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.net.HazardRemovePacket;
import com.eisiadev.avatarofemptiness.net.HazardSyncPacket;
import com.eisiadev.avatarofemptiness.net.ModNetwork;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class HazardManager {
    private final AvatarEntity boss;
    private final List<Hazard> active = new ArrayList<>();
    private final List<Hazard> outSync = new ArrayList<>();
    private final IntArrayList outRemove = new IntArrayList();
    private int nextId = 1;
    private int soundBudget;
    private final int[] thornSoundLast = {-1000, -1000, -1000};

    public HazardManager(AvatarEntity boss) {
        this.boss = boss;
    }

    public Hazard spawn(HazardType type, double x, double y, double z) {
        Hazard h = new Hazard(nextId++, type);
        h.setPos(x, y, z);
        h.seed = boss.getRandom().nextInt();
        active.add(h);
        outSync.add(h);
        return h;
    }

    public void resync(Hazard h) {
        if (!outSync.contains(h)) outSync.add(h);
    }

    public void kill(Hazard h) {
        if (!h.removed) {
            h.removed = true;
            outRemove.add(h.id);
        }
    }

    public List<Hazard> all() {
        return active;
    }

    public int count(HazardType type) {
        int n = 0;
        for (Hazard h : active) if (h.type == type && !h.removed) n++;
        return n;
    }

    public void clearAll(ServerLevel level) {
        active.clear();
        outSync.clear();
        outRemove.clear();
        ModNetwork.sendToLevel(level, new HazardSyncPacket(true, List.of()));
    }

    public void tick(ServerLevel level, List<ServerPlayer> players) {
        soundBudget = 2;
        int now = boss.tickCount;
        Hazard[] snapshot = active.toArray(new Hazard[0]);
        for (Hazard h : snapshot) {
            if (h.removed) continue;
            h.step();
            behave(level, h, players, now);
            if (!h.removed && h.expired()) {
                h.removed = true;
            }
        }
        active.removeIf(h -> h.removed);
        flush(level);
    }

    private void flush(ServerLevel level) {
        if (!outSync.isEmpty()) {
            ModNetwork.sendToLevel(level, new HazardSyncPacket(false, new ArrayList<>(outSync)));
            outSync.clear();
        }
        if (!outRemove.isEmpty()) {
            ModNetwork.sendToLevel(level, new HazardRemovePacket(outRemove.toIntArray()));
            outRemove.clear();
        }
    }

    private void behave(ServerLevel level, Hazard h, List<ServerPlayer> players, int now) {
        switch (h.type) {
            case DISGUSTING_STAR -> {
                if (h.age == h.trigger) {
                    for (ServerPlayer p : players) {
                        if (dist3(p, h.x, h.y, h.z) <= h.size) BossDamage.hit(p, boss, h.damage);
                    }
                    explosionSound(level, h);
                }
            }
            case THORN_PORTAL -> {
                if (h.age == 1) thornSound(players, now, 0, ModSounds.THORN_CHARGE.get());
                if (h.age == h.trigger) thornSound(players, now, 1, ModSounds.THORN_SHOOT.get());
                if (h.age == h.life - BossTuning.THORN_RETRACT_TICKS) thornSound(players, now, 2, ModSounds.THORN_RETRACT.get());
                if (h.age >= h.trigger && h.age < h.life - BossTuning.THORN_RETRACT_TICKS) {
                    for (ServerPlayer p : players) {
                        if (horizontal(p, h.x, h.z) <= BossTuning.THORN_HIT_RADIUS
                                && p.getY() < h.y + BossTuning.THORN_HEIGHT
                                && p.getY() + p.getBbHeight() > h.y - 0.25
                                && h.canHit(p.getUUID(), now, BossTuning.THORN_HIT_INTERVAL)) {
                            BossDamage.hit(p, boss, h.damage);
                        }
                    }
                }
            }
            case SHOCKWAVE -> {
                float radius = h.a * h.age;
                for (ServerPlayer p : players) {
                    double d = horizontal(p, h.x, h.z);
                    if (Math.abs(d - radius) < 1.2 && p.getY() < h.y + BossTuning.SHOCKWAVE_HEIGHT
                            && h.canHit(p.getUUID(), now, 100000)) {
                        BossDamage.hit(p, boss, h.damage);
                    }
                }
            }
            case COMET -> projectileHit(h, players);
            case ICE_CHUNK -> {
                if (h.stage == 0) {
                    if (h.age >= h.trigger) aimChunk(level, h, players);
                } else {
                    projectileHit(h, players);
                }
            }
            case DANGER_ZONE -> {
                if (h.age == h.trigger) {
                    for (ServerPlayer p : players) {
                        if (dist3(p, h.x, h.y, h.z) <= h.size) BossDamage.hitPercent(p, boss, h.a);
                    }
                }
            }
            case DEATHRAY -> {
                double ang = h.a + h.b * h.age;
                double dx = Math.cos(ang), dz = Math.sin(ang);
                for (ServerPlayer p : players) {
                    double rx = p.getX() - h.x, rz = p.getZ() - h.z;
                    double along = rx * dx + rz * dz;
                    double perp = Math.abs(rx * dz - rz * dx);
                    if (along > 0 && along < h.size && perp < BossTuning.DEATHRAY_HALF_WIDTH
                            && p.getY() < h.y + BossTuning.DEATHRAY_HEIGHT) {
                        BossDamage.kill(p, boss);
                    }
                }
            }
            case SUBZERO_PORTAL -> {
                if (h.age >= h.trigger) {
                    for (ServerPlayer p : players) {
                        if (horizontal(p, h.x, h.z) < h.size && p.getY() < h.y + BossTuning.TORNADO_HEIGHT) {
                            BossDamage.kill(p, boss);
                        }
                    }
                    int since = h.age - h.trigger;
                    if (since % 20 == 0 && h.age < h.life - 10) emitChunks(h);
                }
            }
            case BLACKHOLE -> {
                for (ServerPlayer p : players) {
                    if (dist3(p, h.x, h.y, h.z) < h.size
                            && h.canHit(p.getUUID(), now, BossTuning.BLACKHOLE_HIT_INTERVAL)) {
                        BossDamage.hitPercent(p, boss, BossTuning.BLACKHOLE_PERCENT);
                    }
                }
            }
            case STAR_WHITE, STAR_BLUE, STAR_RED -> {
                for (ServerPlayer p : players) {
                    if (dist3(p, h.x, h.y, h.z) < h.size
                            && h.canHit(p.getUUID(), now, BossTuning.STAR_HIT_INTERVAL)) {
                        BossDamage.hit(p, boss, h.damage);
                    }
                }
            }
            case PARADISE_FLOOR -> {
                for (ServerPlayer p : players) {
                    if (p.getY() <= h.y) BossDamage.kill(p, boss);
                }
            }
            default -> {
            }
        }
    }

    private void projectileHit(Hazard h, List<ServerPlayer> players) {
        Vec3 from = new Vec3(h.prevX, h.prevY, h.prevZ);
        Vec3 to = new Vec3(h.x, h.y, h.z);
        for (ServerPlayer p : players) {
            AABB box = p.getBoundingBox().inflate(h.size);
            if (box.contains(to) || box.clip(from, to).isPresent()) {
                BossDamage.hit(p, boss, h.damage);
                kill(h);
                return;
            }
        }
    }

    private void emitChunks(Hazard portal) {
        RandomSource r = boss.getRandom();
        for (int i = 0; i < BossTuning.TORNADO_CHUNKS_PER_EMIT; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0;
            Hazard c = spawn(HazardType.ICE_CHUNK, portal.x, portal.y + 2.0 + r.nextDouble() * 6.0, portal.z);
            c.setVel(Math.cos(ang) * 0.55, 1.0 + r.nextDouble() * 0.4, Math.sin(ang) * 0.55);
            c.velMul = 0.8f;
            c.trigger = 25;
            c.life = 25 + 50;
            c.size = 0.6f;
            c.damage = BossTuning.SUBZERO_CHUNK_DAMAGE;
        }
    }

    private void aimChunk(ServerLevel level, Hazard h, List<ServerPlayer> players) {
        ServerPlayer target = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer p : players) {
            double d = dist3(p, h.x, h.y, h.z);
            if (d < best) {
                best = d;
                target = p;
            }
        }
        h.stage = 1;
        h.velMul = 1.0f;
        if (target == null) {
            h.setVel(0, -BossTuning.SUBZERO_CHUNK_SPEED, 0);
        } else {
            Vec3 aim = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(h.x, h.y, h.z).normalize()
                    .scale(BossTuning.SUBZERO_CHUNK_SPEED);
            h.setVel(aim.x, aim.y, aim.z);
        }
        resync(h);
        if (soundBudget-- > 0) {
            level.playSound(null, h.x, h.y, h.z, ModSounds.vanilla("entity.ghast.shoot"), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
    }

    private void thornSound(List<ServerPlayer> players, int now, int kind, net.minecraft.sounds.SoundEvent sound) {
        if (now - thornSoundLast[kind] < BossTuning.THORN_SOUND_WINDOW) return;
        thornSoundLast[kind] = now;
        for (ServerPlayer p : players) p.playNotifySound(sound, SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    private void explosionSound(ServerLevel level, Hazard h) {
        if (soundBudget-- > 0) {
            level.playSound(null, h.x, h.y, h.z, ModSounds.BLOOD_EX.get(), SoundSource.HOSTILE, 4.0f, 1.0f);
        }
    }

    private static double dist3(ServerPlayer p, double x, double y, double z) {
        double dx = p.getX() - x;
        double dy = p.getY() + p.getBbHeight() * 0.5 - y;
        double dz = p.getZ() - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double horizontal(ServerPlayer p, double x, double z) {
        return Arena.horizontalDistance(p.getX(), p.getZ(), x, z);
    }
}
