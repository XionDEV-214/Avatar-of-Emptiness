package com.eisiadev.avatarofemptiness.boss;

import com.eisiadev.avatarofemptiness.AoeConfig;
import com.eisiadev.avatarofemptiness.registry.ModDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class BossDamage {
    public static void hit(ServerPlayer player, @Nullable Entity attacker, float amount) {
        if (!valid(player)) return;
        float scaled = (float) (amount * AoeConfig.DAMAGE_MULTIPLIER.get());
        if (scaled <= 0f) return;
        player.invulnerableTime = 0;
        player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.AVATAR, attacker), scaled);
        player.invulnerableTime = 0;
    }

    public static void hitPercent(ServerPlayer player, @Nullable Entity attacker, float fraction) {
        if (!valid(player)) return;
        player.invulnerableTime = 0;
        player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.AVATAR_TRUE, attacker), player.getMaxHealth() * fraction);
        player.invulnerableTime = 0;
    }

    public static void kill(ServerPlayer player, @Nullable Entity attacker) {
        if (!valid(player)) return;
        player.invulnerableTime = 0;
        float lethal = Math.max(1.0e7f, player.getMaxHealth() * 100f + player.getAbsorptionAmount() * 100f);
        player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.AVATAR_TRUE, attacker), lethal);
        player.invulnerableTime = 0;
    }

    private static boolean valid(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator();
    }

    private BossDamage() {}
}
