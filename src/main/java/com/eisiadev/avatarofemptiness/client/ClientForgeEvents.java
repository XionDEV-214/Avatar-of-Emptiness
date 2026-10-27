package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.AoeConfig;
import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.client.render.WorldFxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AvatarOfEmptinessMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.isPaused()) return;
        ClientHazards.tick();
        ClientArenaState.tick();
        ClientCutscene.tick();
        BossMusicPlayer.tick();
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        WorldFxRenderer.render(event);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float shake = ClientArenaState.shake() * AoeConfig.SHAKE_INTENSITY.get().floatValue();
        if (shake <= 0.001f) return;
        double t = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) + event.getPartialTick();
        float yaw = (float) (Math.sin(t * 2.9) + Math.sin(t * 7.3) * 0.5) * shake;
        float pitch = (float) (Math.cos(t * 3.7) + Math.sin(t * 6.1) * 0.5) * shake;
        float roll = (float) Math.sin(t * 5.3) * shake * 0.6f;
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHazards.clear();
        ClientArenaState.reset();
        ClientCutscene.reset();
        BossMusicPlayer.stop();
    }

    private ClientForgeEvents() {}
}
