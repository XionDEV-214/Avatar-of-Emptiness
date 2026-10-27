package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Collection;
import java.util.List;

public final class ClientHazards {
    private static final Int2ObjectOpenHashMap<Hazard> MAP = new Int2ObjectOpenHashMap<>();

    public static void sync(boolean clear, List<Hazard> hazards) {
        if (clear) MAP.clear();
        for (Hazard h : hazards) MAP.put(h.id, h);
    }

    public static void remove(int[] ids) {
        for (int id : ids) MAP.remove(id);
    }

    public static void tick() {
        ObjectIterator<Hazard> it = MAP.values().iterator();
        while (it.hasNext()) {
            Hazard h = it.next();
            h.step();
            if (h.type == HazardType.THORN_PORTAL && h.age == h.trigger) thornShake(h);
            if (h.expired()) it.remove();
        }
    }

    private static void thornShake(Hazard h) {
        Player p = Minecraft.getInstance().player;
        if (p == null) return;
        double d = Math.sqrt(p.distanceToSqr(h.x, h.y, h.z));
        if (d < 28.0) ClientArenaState.addShake((float) (1.1 * (1.0 - d / 28.0)));
    }

    public static Collection<Hazard> all() {
        return MAP.values();
    }

    public static boolean isEmpty() {
        return MAP.isEmpty();
    }

    public static void clear() {
        MAP.clear();
    }

    private ClientHazards() {}
}
