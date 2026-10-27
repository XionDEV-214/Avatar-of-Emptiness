package com.eisiadev.avatarofemptiness.registry;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> AVATAR =
            ResourceKey.create(Registries.DAMAGE_TYPE, AvatarOfEmptinessMod.id("avatar"));
    public static final ResourceKey<DamageType> AVATAR_TRUE =
            ResourceKey.create(Registries.DAMAGE_TYPE, AvatarOfEmptinessMod.id("avatar_true"));

    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), attacker);
    }

    private ModDamageTypes() {}
}
