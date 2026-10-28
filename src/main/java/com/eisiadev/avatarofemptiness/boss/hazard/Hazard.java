package com.eisiadev.avatarofemptiness.boss.hazard;

import com.eisiadev.avatarofemptiness.arena.Arena;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hazard {
    public final int id;
    public final HazardType type;

    public float x, y, z;
    public float prevX, prevY, prevZ;
    public float vx, vy, vz;
    public float velMul = 1.0f;
    public float pull = 0.0f;
    public float maxSpeed = 0.0f;
    public float tx, ty, tz;

    public float size = 1.0f;
    public float a, b;
    public int trigger;
    public int life = 20;
    public int age;
    public int seed;

    public boolean removed;

    public float damage;
    public int stage;
    private Map<UUID, Integer> lastHit;

    public Hazard(int id, HazardType type) {
        this.id = id;
        this.type = type;
    }

    public void setPos(double px, double py, double pz) {
        this.x = (float) px;
        this.y = (float) py;
        this.z = (float) pz;
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
    }

    public void setVel(double dx, double dy, double dz) {
        this.vx = (float) dx;
        this.vy = (float) dy;
        this.vz = (float) dz;
    }

    public void setTarget(double px, double py, double pz) {
        this.tx = (float) px;
        this.ty = (float) py;
        this.tz = (float) pz;
    }

    public void step() {
        prevX = x;
        prevY = y;
        prevZ = z;
        if (pull != 0.0f) {
            float dx = tx - x, dy = ty - y, dz = tz - z;
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 1.0e-4f) {
                vx += dx / len * pull;
                vy += dy / len * pull;
                vz += dz / len * pull;
            }
        }
        vx *= velMul;
        vy *= velMul;
        vz *= velMul;
        if (maxSpeed > 0.0f) {
            float sp = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (sp > maxSpeed) {
                float s = maxSpeed / sp;
                vx *= s;
                vy *= s;
                vz *= s;
            }
        }
        x += vx;
        y += vy;
        z += vz;
        age++;
    }

    public boolean expired() {
        if (age >= life) return true;
        if (type.isStar()) {
            float dx = tx - x, dy = ty - y, dz = tz - z;
            return dx * dx + dy * dy + dz * dz < 4.0f;
        }
        if (type == HazardType.COMET && b > 0.5f) {
            float dx = tx - x, dz = tz - z;
            if (dx * dx + dz * dz < 6.25f) return true;
        }
        if (type.isProjectile()) {
            return !Arena.inside(x, y, z, -24.0);
        }
        return false;
    }

    public float lerpX(float pt) { return prevX + (x - prevX) * pt; }
    public float lerpY(float pt) { return prevY + (y - prevY) * pt; }
    public float lerpZ(float pt) { return prevZ + (z - prevZ) * pt; }

    public boolean canHit(UUID player, int now, int interval) {
        if (lastHit == null) lastHit = new HashMap<>();
        Integer last = lastHit.get(player);
        if (last != null && now - last < interval) return false;
        lastHit.put(player, now);
        return true;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeVarInt(type.ordinal());
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(vx);
        buf.writeFloat(vy);
        buf.writeFloat(vz);
        buf.writeFloat(velMul);
        buf.writeFloat(pull);
        buf.writeFloat(maxSpeed);
        buf.writeFloat(tx);
        buf.writeFloat(ty);
        buf.writeFloat(tz);
        buf.writeFloat(size);
        buf.writeFloat(a);
        buf.writeFloat(b);
        buf.writeVarInt(trigger);
        buf.writeVarInt(life);
        buf.writeVarInt(age);
        buf.writeInt(seed);
    }

    public static Hazard read(FriendlyByteBuf buf) {
        Hazard h = new Hazard(buf.readVarInt(), HazardType.byId(buf.readVarInt()));
        h.x = buf.readFloat();
        h.y = buf.readFloat();
        h.z = buf.readFloat();
        h.prevX = h.x;
        h.prevY = h.y;
        h.prevZ = h.z;
        h.vx = buf.readFloat();
        h.vy = buf.readFloat();
        h.vz = buf.readFloat();
        h.velMul = buf.readFloat();
        h.pull = buf.readFloat();
        h.maxSpeed = buf.readFloat();
        h.tx = buf.readFloat();
        h.ty = buf.readFloat();
        h.tz = buf.readFloat();
        h.size = buf.readFloat();
        h.a = buf.readFloat();
        h.b = buf.readFloat();
        h.trigger = buf.readVarInt();
        h.life = buf.readVarInt();
        h.age = buf.readVarInt();
        h.seed = buf.readInt();
        return h;
    }
}
