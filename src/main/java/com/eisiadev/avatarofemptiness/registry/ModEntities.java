package com.eisiadev.avatarofemptiness.registry;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.VisceraEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AvatarOfEmptinessMod.MODID);

    public static final RegistryObject<EntityType<AvatarEntity>> AVATAR = ENTITIES.register("avatar_of_emptiness",
            () -> EntityType.Builder.<AvatarEntity>of(AvatarEntity::new, MobCategory.MONSTER)
                    .sized(5.0f, 7.0f)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("avatar_of_emptiness"));

    public static final RegistryObject<EntityType<VisceraEntity>> VISCERA = ENTITIES.register("viscera",
            () -> EntityType.Builder.<VisceraEntity>of(VisceraEntity::new, MobCategory.MONSTER)
                    .sized(2.5f, 2.5f)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("viscera"));

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(AVATAR.get(), AvatarEntity.createAttributes().build());
        event.put(VISCERA.get(), VisceraEntity.createAttributes().build());
    }

    private ModEntities() {}
}
