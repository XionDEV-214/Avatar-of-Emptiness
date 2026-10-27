package com.eisiadev.avatarofemptiness;

import com.eisiadev.avatarofemptiness.net.ModNetwork;
import com.eisiadev.avatarofemptiness.registry.ModEntities;
import com.eisiadev.avatarofemptiness.registry.ModItems;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AvatarOfEmptinessMod.MODID)
public class AvatarOfEmptinessMod {
    public static final String MODID = "avatarofemptiness";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AvatarOfEmptinessMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.SOUNDS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(ModEntities::onAttributes);
        modBus.addListener(ModItems::onCreativeTab);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AoeConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AoeConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
