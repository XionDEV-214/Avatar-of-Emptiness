package com.eisiadev.avatarofemptiness.arena;

import com.eisiadev.avatarofemptiness.net.CutscenePacket;
import com.eisiadev.avatarofemptiness.net.ModNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CutsceneManager {
    public static final int DURATION = 601;

    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();

    public static boolean isActive(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void start(ServerPlayer player) {
        ACTIVE.put(player.getUUID(), 0);
        ModNetwork.sendTo(player, new CutscenePacket(DURATION));
    }

    public static void cancel(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) != null) {
            ModNetwork.sendTo(player, new CutscenePacket(0));
        }
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;
        Iterator<Map.Entry<UUID, Integer>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }
            int t = e.getValue() + 1;
            if (t >= DURATION) {
                it.remove();
                ArenaFight.startFor(player);
            } else {
                e.setValue(t);
            }
        }
    }

    private CutsceneManager() {}
}
