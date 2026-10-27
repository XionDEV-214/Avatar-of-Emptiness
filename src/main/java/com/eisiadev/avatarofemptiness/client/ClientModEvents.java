package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.client.model.BbModels;
import com.eisiadev.avatarofemptiness.client.render.InvisibleRenderer;
import com.eisiadev.avatarofemptiness.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = AvatarOfEmptinessMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onShaders(RegisterShadersEvent event) throws IOException {
        ModShaders.register(event);
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AVATAR.get(), InvisibleRenderer::new);
        event.registerEntityRenderer(ModEntities.VISCERA.get(), InvisibleRenderer::new);
    }

    @SubscribeEvent
    public static void onDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(AvatarOfEmptinessMod.id("arena"), new ArenaSkyEffects());
    }

    @SubscribeEvent
    public static void onOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("aoe_eclipse", FlashOverlay::renderEclipse);
        event.registerAboveAll("aoe_flash", FlashOverlay::render);
    }

    @SubscribeEvent
    public static void onReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(BbModels.RELOAD_LISTENER);
    }

    private ClientModEvents() {}
}
