package com.eisiadev.avatarofemptiness.arena;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ArenaFight {

    @Nullable
    public static ServerLevel arena(MinecraftServer server) {
        return server.getLevel(Arena.LEVEL);
    }

    @Nullable
    public static AvatarEntity findBoss(ServerLevel arena) {
        List<AvatarEntity> list = arena.getEntitiesOfClass(AvatarEntity.class,
                new AABB(Arena.CENTER, Arena.CENTER).inflate(Arena.RADIUS + 16));
        return list.isEmpty() ? null : list.get(0);
    }

    public static void startFor(ServerPlayer player) {
        ServerLevel arena = arena(player.server);
        if (arena == null) {
            player.sendSystemMessage(Component.literal("[Avatar of Emptiness] arena dimension is missing (datapack not loaded?)"));
            AvatarOfEmptinessMod.LOGGER.error("Arena dimension {} not found", Arena.LEVEL.location());
            return;
        }
        arena.getChunk(0, 0);

        player.teleportTo(arena, Arena.PLAYER_START.x, Arena.PLAYER_START.y, Arena.PLAYER_START.z,
                Arena.PLAYER_START_YAW, 0f);
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0;

        AvatarEntity boss = findBoss(arena);
        if (boss == null) {
            boss = ModEntities.AVATAR.get().create(arena);
            if (boss == null) return;
            boss.moveTo(Arena.CX, Arena.FLOOR_Y, Arena.CZ, 0f, 0f);
            boss.setYHeadRot(0f);
            arena.addFreshEntity(boss);
        }
    }

    public static void onVictory(ServerLevel arena, AvatarEntity boss) {
        for (ServerPlayer p : arena.players()) {
            p.sendSystemMessage(boss.phase().displayName());
        }
    }

    public static void sendHome(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        BlockPos top = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
        player.teleportTo(overworld, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0;
    }

    private ArenaFight() {}
}
